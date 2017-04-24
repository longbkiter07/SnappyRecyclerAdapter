package me.silong.snappyadapter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by SILONG on 4/20/17.
 */

class SnappySortedList<T> {

  /**
   * Used by {@link #indexOf(Object)} when he item cannot be found in the list.
   */
  public static final int INVALID_POSITION = -1;

  private static final int MIN_CAPACITY = 10;

  private static final int CAPACITY_GROWTH = MIN_CAPACITY;

  private static final int INSERTION = 1;

  private static final int DELETION = 1 << 1;

  private static final int LOOKUP = 1 << 2;

  private final Class<T> mTClass;

  T[] mData;

  private T[] mOldData;

  private int mOldDataStart;

  private int mOldDataSize;

  private int mMergedSize;

  private RxSortedListCallback<T> mCallback;

  private RxSortedListCallback.BatchedCallback mBatchedCallback;

  private int mSize;

  public SnappySortedList(Class<T> klass, RxSortedListCallback<T> callback) {
    this(klass, callback, MIN_CAPACITY);
  }

  public SnappySortedList(Class<T> klass, RxSortedListCallback<T> callback, int initialCapacity) {
    mTClass = klass;
    mData = (T[]) Array.newInstance(klass, initialCapacity);
    mCallback = callback;
    mSize = 0;
  }

  public int size() {
    return mSize;
  }

  public int add(T item) {
    throwIfMerging();
    return add(item, true);
  }

  public void addAll(T[] items, boolean mayModifyInput) {
    throwIfMerging();
    if (items.length == 0) {
      return;
    }
    if (mayModifyInput) {
      addAllInternal(items);
    } else {
      T[] copy = (T[]) Array.newInstance(mTClass, items.length);
      System.arraycopy(items, 0, copy, 0, items.length);
      addAllInternal(copy);
    }

  }

  public void addAll(T... items) {
    addAll(items, false);
  }

  public void addAll(Collection<T> items) {
    T[] copy = (T[]) Array.newInstance(mTClass, items.size());
    addAll(items.toArray(copy), true);
  }

  public Observable<Void> set(Collection<T> items) {
    return set(items, false);
  }

  public Observable<Void> set(Collection<T> items, boolean isSorted) {
    return Observable.fromCallable(() -> {
      T[] newItems = (T[]) Array.newInstance(mTClass, items.size());
      T[] oldItems = (T[]) Array.newInstance(mTClass, mData.length);
      System.arraycopy(mData, 0, oldItems, 0, mData.length);
      items.toArray(newItems);
      if (!isSorted) {
        Arrays.sort(newItems, mCallback);
      }
      return SnappyDiffCallback.calculate(mCallback, oldItems, newItems, mSize, items.size());
    })
        .observeOn(AndroidSchedulers.mainThread())
        .map(diffResultPair -> {
          mData = diffResultPair.second;
          mSize = diffResultPair.second.length;
          diffResultPair.first.dispatchUpdatesTo(mCallback);
          return null;
        });
  }

  private void addAllInternal(T[] newItems) {
    final boolean forceBatchedUpdates = !(mCallback instanceof RxSortedListCallback.BatchedCallback);
    if (forceBatchedUpdates) {
      beginBatchedUpdates();
    }

    mOldData = mData;
    mOldDataStart = 0;
    mOldDataSize = mSize;

    Arrays.sort(newItems, mCallback);  // Arrays.sort is stable.

    final int newSize = deduplicate(newItems);
    if (mSize == 0) {
      mData = newItems;
      mSize = newSize;
      mMergedSize = newSize;
      mCallback.onInserted(0, newSize);
    } else {
      merge(newItems, newSize);
    }

    mOldData = null;

    if (forceBatchedUpdates) {
      endBatchedUpdates();
    }
  }

  private int deduplicate(T[] items) {
    if (items.length == 0) {
      throw new IllegalArgumentException("Input array must be non-empty");
    }

    // Keep track of the range of equal items at the end of the output.
    // Start with the range containing just the first item.
    int rangeStart = 0;
    int rangeEnd = 1;

    for (int i = 1; i < items.length; ++i) {
      T currentItem = items[i];

      int compare = mCallback.compare(items[rangeStart], currentItem);
      if (compare > 0) {
        throw new IllegalArgumentException("Input must be sorted in ascending order.");
      }

      if (compare == 0) {
        // The range of equal items continues, update it.
        final int sameItemPos = findSameItem(currentItem, items, rangeStart, rangeEnd);
        if (sameItemPos != INVALID_POSITION) {
          // Replace the duplicate item.
          items[sameItemPos] = currentItem;
        } else {
          // Expand the range.
          if (rangeEnd != i) {  // Avoid redundant copy.
            items[rangeEnd] = currentItem;
          }
          rangeEnd++;
        }
      } else {
        // The range has ended. Reset it to contain just the current item.
        if (rangeEnd != i) {  // Avoid redundant copy.
          items[rangeEnd] = currentItem;
        }
        rangeStart = rangeEnd++;
      }
    }
    return rangeEnd;
  }


  private int findSameItem(T item, T[] items, int from, int to) {
    for (int pos = from; pos < to; pos++) {
      if (mCallback.areItemsTheSame(items[pos], item)) {
        return pos;
      }
    }
    return INVALID_POSITION;
  }

  /**
   * This method assumes that newItems are sorted and deduplicated.
   */
  private void merge(T[] newData, int newDataSize) {
    final int mergedCapacity = mSize + newDataSize + CAPACITY_GROWTH;
    mData = (T[]) Array.newInstance(mTClass, mergedCapacity);
    mMergedSize = 0;

    int newDataStart = 0;
    while (mOldDataStart < mOldDataSize || newDataStart < newDataSize) {
      if (mOldDataStart == mOldDataSize) {
        // No more old items, copy the remaining new items.
        int itemCount = newDataSize - newDataStart;
        System.arraycopy(newData, newDataStart, mData, mMergedSize, itemCount);
        mMergedSize += itemCount;
        mSize += itemCount;
        mCallback.onInserted(mMergedSize - itemCount, itemCount);
        break;
      }

      if (newDataStart == newDataSize) {
        // No more new items, copy the remaining old items.
        int itemCount = mOldDataSize - mOldDataStart;
        System.arraycopy(mOldData, mOldDataStart, mData, mMergedSize, itemCount);
        mMergedSize += itemCount;
        break;
      }

      T oldItem = mOldData[mOldDataStart];
      T newItem = newData[newDataStart];
      int compare = mCallback.compare(oldItem, newItem);
      if (compare > 0) {
        // New item is lower, output it.
        mData[mMergedSize++] = newItem;
        mSize++;
        newDataStart++;
        mCallback.onInserted(mMergedSize - 1, 1);
      } else if (compare == 0 && mCallback.areItemsTheSame(oldItem, newItem)) {
        // Items are the same. Output the new item, but consume both.
        mData[mMergedSize++] = newItem;
        newDataStart++;
        mOldDataStart++;
        if (!mCallback.areContentsTheSame(oldItem, newItem)) {
          mCallback.onChanged(mMergedSize - 1, 1);
        }
      } else {
        // Old item is lower than or equal to (but not the same as the new). Output it.
        // New item with the same sort order will be inserted later.
        mData[mMergedSize++] = oldItem;
        mOldDataStart++;
      }
    }
  }

  private void throwIfMerging() {
    if (mOldData != null) {
      throw new IllegalStateException("Cannot call this method from within addAll");
    }
  }

  public void beginBatchedUpdates() {
    throwIfMerging();
    if (mCallback instanceof RxSortedListCallback.BatchedCallback) {
      return;
    }
    if (mBatchedCallback == null) {
      mBatchedCallback = new RxSortedListCallback.BatchedCallback(mCallback);
    }
    mCallback = mBatchedCallback;
  }

  public void endBatchedUpdates() {
    throwIfMerging();
    if (mCallback instanceof RxSortedListCallback.BatchedCallback) {
      ((RxSortedListCallback.BatchedCallback) mCallback).dispatchLastEvent();
    }
    if (mCallback == mBatchedCallback) {
      mCallback = mBatchedCallback.mWrappedCallback;
    }
  }

  private int add(T item, boolean notify) {
    int index = findIndexOf(item, mData, 0, mSize, INSERTION);
    if (index == INVALID_POSITION) {
      index = 0;
    } else if (index < mSize) {
      T existing = mData[index];
      if (mCallback.areItemsTheSame(existing, item)) {
        if (mCallback.areContentsTheSame(existing, item)) {
          //no change but still replace the item
          mData[index] = item;
          return index;
        } else {
          mData[index] = item;
          mCallback.onChanged(index, 1);
          return index;
        }
      }
    }
    addToData(index, item);
    if (notify) {
      mCallback.onInserted(index, 1);
    }
    return index;
  }

  public boolean remove(T item) {
    throwIfMerging();
    return remove(item, true);
  }

  public T removeItemAt(int index) {
    throwIfMerging();
    T item = get(index);
    removeItemAtIndex(index, true);
    return item;
  }

  private boolean remove(T item, boolean notify) {
    int index = findIndexOf(item, mData, 0, mSize, DELETION);
    if (index == INVALID_POSITION) {
      return false;
    }
    removeItemAtIndex(index, notify);
    return true;
  }

  private void removeItemAtIndex(int index, boolean notify) {
    System.arraycopy(mData, index + 1, mData, index, mSize - index - 1);
    mSize--;
    mData[mSize] = null;
    if (notify) {
      mCallback.onRemoved(index, 1);
    }
  }

  public void updateItemAt(int index, T item) {
    throwIfMerging();
    final T existing = get(index);
    // assume changed if the same object is given back
    boolean contentsChanged = existing == item || !mCallback.areContentsTheSame(existing, item);
    if (existing != item) {
      // different items, we can use comparison and may avoid lookup
      final int cmp = mCallback.compare(existing, item);
      if (cmp == 0) {
        mData[index] = item;
        if (contentsChanged) {
          mCallback.onChanged(index, 1);
        }
        return;
      }
    }
    if (contentsChanged) {
      mCallback.onChanged(index, 1);
    }
    // TODO this done in 1 pass to avoid shifting twice.
    removeItemAtIndex(index, false);
    int newIndex = add(item, false);
    if (index != newIndex) {
      mCallback.onMoved(index, newIndex);
    }
  }

  public void recalculatePositionOfItemAt(int index) {
    throwIfMerging();
    // TODO can be improved
    final T item = get(index);
    removeItemAtIndex(index, false);
    int newIndex = add(item, false);
    if (index != newIndex) {
      mCallback.onMoved(index, newIndex);
    }
  }

  public T get(int index) throws IndexOutOfBoundsException {
    if (index >= mSize || index < 0) {
      throw new IndexOutOfBoundsException("Asked to get item at " + index + " but size is "
          + mSize);
    }
    if (mOldData != null) {
      // The call is made from a callback during addAll execution. The data is split
      // between mData and mOldData.
      if (index >= mMergedSize) {
        return mOldData[index - mMergedSize + mOldDataStart];
      }
    }
    return mData[index];
  }

  public int indexOf(T item) {
    if (mOldData != null) {
      int index = findIndexOf(item, mData, 0, mMergedSize, LOOKUP);
      if (index != INVALID_POSITION) {
        return index;
      }
      index = findIndexOf(item, mOldData, mOldDataStart, mOldDataSize, LOOKUP);
      if (index != INVALID_POSITION) {
        return index - mOldDataStart + mMergedSize;
      }
      return INVALID_POSITION;
    }
    return findIndexOf(item, mData, 0, mSize, LOOKUP);
  }

  private int findIndexOf(T item, T[] mData, int left, int right, int reason) {
    while (left < right) {
      final int middle = (left + right) / 2;
      T myItem = mData[middle];
      final int cmp = mCallback.compare(myItem, item);
      if (cmp < 0) {
        left = middle + 1;
      } else if (cmp == 0) {
        if (mCallback.areItemsTheSame(myItem, item)) {
          return middle;
        } else {
          int exact = linearEqualitySearch(item, middle, left, right);
          if (reason == INSERTION) {
            return exact == INVALID_POSITION ? middle : exact;
          } else {
            return exact;
          }
        }
      } else {
        right = middle;
      }
    }
    return reason == INSERTION ? left : INVALID_POSITION;
  }

  private int linearEqualitySearch(T item, int middle, int left, int right) {
    // go left
    for (int next = middle - 1; next >= left; next--) {
      T nextItem = mData[next];
      int cmp = mCallback.compare(nextItem, item);
      if (cmp != 0) {
        break;
      }
      if (mCallback.areItemsTheSame(nextItem, item)) {
        return next;
      }
    }
    for (int next = middle + 1; next < right; next++) {
      T nextItem = mData[next];
      int cmp = mCallback.compare(nextItem, item);
      if (cmp != 0) {
        break;
      }
      if (mCallback.areItemsTheSame(nextItem, item)) {
        return next;
      }
    }
    return INVALID_POSITION;
  }

  private void addToData(int index, T item) {
    if (index > mSize) {
      throw new IndexOutOfBoundsException(
          "cannot add item to " + index + " because size is " + mSize);
    }
    if (mSize == mData.length) {
      // we are at the limit enlarge
      T[] newData = (T[]) Array.newInstance(mTClass, mData.length + CAPACITY_GROWTH);
      System.arraycopy(mData, 0, newData, 0, index);
      newData[index] = item;
      System.arraycopy(mData, index, newData, index + 1, mSize - index);
      mData = newData;
    } else {
      // just shift, we fit
      System.arraycopy(mData, index, mData, index + 1, mSize - index);
      mData[index] = item;
    }
    mSize++;
  }

  /**
   * Removes all items from the SortedList.
   */
  public void clear() {
    throwIfMerging();
    if (mSize == 0) {
      return;
    }
    final int prevSize = mSize;
    Arrays.fill(mData, 0, prevSize, null);
    mSize = 0;
    mCallback.onRemoved(0, prevSize);
  }

}