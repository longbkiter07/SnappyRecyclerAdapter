package me.silong.snappyadapter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by SILONG on 4/20/17.
 */

/**
 * A Sorted list implementation that can keep items in order and also notify for changes in the
 * list
 * such that it can be bound to a {@link android.support.v7.widget.RecyclerView.Adapter
 * RecyclerView.Adapter}.
 * <p>
 * It keeps items ordered using the {@link android.support.v7.util.SortedList.Callback#compare(Object, Object)} method and uses
 * binary search to retrieve items. If the sorting criteria of your items may change, make sure you
 * call appropriate methods while editing them to avoid data inconsistencies.
 * <p>
 * You can control the order of items and change notifications via the {@link android.support.v7.util.SortedList.Callback} parameter.
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

  /**
   * A copy of the previous list contents used during the merge phase of addAll.
   */
  private T[] mOldData;

  private int mOldDataStart;

  private int mOldDataSize;

  /**
   * The size of the valid portion of mData during the merge phase of addAll.
   */
  private int mMergedSize;

  /**
   * The callback instance that controls the behavior of the SortedList and get notified when
   * changes happen.
   */
  private RxSortedListCallback<T> mCallback;

  private RxSortedListCallback.BatchedCallback mBatchedCallback;

  private int mSize;

  /**
   * Creates a new SortedList of type T.
   *
   * @param klass    The class of the contents of the SortedList.
   * @param callback The callback that controls the behavior of SortedList.
   */
  public SnappySortedList(Class<T> klass, RxSortedListCallback<T> callback) {
    this(klass, callback, MIN_CAPACITY);
  }

  /**
   * Creates a new SortedList of type T.
   *
   * @param klass           The class of the contents of the SortedList.
   * @param callback        The callback that controls the behavior of SortedList.
   * @param initialCapacity The initial capacity to hold items.
   */
  public SnappySortedList(Class<T> klass, RxSortedListCallback<T> callback, int initialCapacity) {
    mTClass = klass;
    mData = (T[]) Array.newInstance(klass, initialCapacity);
    mCallback = callback;
    mSize = 0;
  }

  /**
   * The number of items in the list.
   *
   * @return The number of items in the list.
   */
  public int size() {
    return mSize;
  }

  /**
   * Adds the given item to the list. If this is a new item, SortedList calls
   * {@link android.support.v7.util.SortedList.Callback#onInserted(int, int)}.
   * <p>
   * If the item already exists in the list and its sorting criteria is not changed, it is
   * replaced with the existing Item. SortedList uses
   * {@link android.support.v7.util.SortedList.Callback#areItemsTheSame(Object, Object)} to check if two items are the same item
   * and uses {@link android.support.v7.util.SortedList.Callback#areContentsTheSame(Object, Object)} to decide whether it should
   * call {@link android.support.v7.util.SortedList.Callback#onChanged(int, int)} or not. In both cases, it always removes the
   * reference to the old item and puts the new item into the backing array even if
   * {@link android.support.v7.util.SortedList.Callback#areContentsTheSame(Object, Object)} returns false.
   * <p>
   * If the sorting criteria of the item is changed, SortedList won't be able to find
   * its duplicate in the list which will result in having a duplicate of the Item in the list.
   * If you need to update sorting criteria of an item that already exists in the list,
   * use {@link #updateItemAt(int, Object)}. You can find the index of the item using
   * {@link #indexOf(Object)} before you update the object.
   *
   * @param item The item to be added into the list.
   * @return The index of the newly added item.
   * @see {@link android.support.v7.util.SortedList.Callback#compare(Object, Object)}
   * @see {@link android.support.v7.util.SortedList.Callback#areItemsTheSame(Object, Object)}
   * @see {@link android.support.v7.util.SortedList.Callback#areContentsTheSame(Object, Object)}}
   */
  public int add(T item) {
    throwIfMerging();
    return add(item, true);
  }

  /**
   * Adds the given items to the list. Equivalent to calling {@link android.support.v7.util.SortedList#add} in a loop,
   * except the callback events may be in a different order/granularity since addAll can batch
   * them for better performance.
   * <p>
   * If allowed, may modify the input array and even take the ownership over it in order
   * to avoid extra memory allocation during sorting and deduplication.
   * </p>
   *
   * @param items          Array of items to be added into the list.
   * @param mayModifyInput If true, SortedList is allowed to modify the input.
   * @see {@link android.support.v7.util.SortedList#addAll(Object[] items)}.
   */
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

  /**
   * Adds the given items to the list. Does not modify the input.
   *
   * @param items Array of items to be added into the list.
   * @see {@link android.support.v7.util.SortedList#addAll(T[] items, boolean mayModifyInput)}
   */
  public void addAll(T... items) {
    addAll(items, false);
  }

  /**
   * Adds the given items to the list. Does not modify the input.
   *
   * @param items Collection of items to be added into the list.
   * @see {@link android.support.v7.util.SortedList#addAll(T[] items, boolean mayModifyInput)}
   */
  public void addAll(Collection<T> items) {
    T[] copy = (T[]) Array.newInstance(mTClass, items.size());
    addAll(items.toArray(copy), true);
  }

  /**
   * Set items using DiffUtil
   *
   * @param items Collection of items to be set into the list.
   */
  public Observable<Void> set(Collection<T> items) {
    return set(items, false);
  }

  /**
   * Set items using DiffUtil
   *
   * @param items    Collection of items to be set into the list.
   * @param isSorted whether items is sorted or not
   */
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

  /**
   * Remove duplicate items, leaving only the last item from each group of "same" items.
   * Move the remaining items to the beginning of the array.
   *
   * @return Number of deduplicated items at the beginning of the array.
   */
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

  /**
   * Batches adapter updates that happen between calling this method until calling
   * {@link #endBatchedUpdates()}. For example, if you add multiple items in a loop
   * and they are placed into consecutive indices, SortedList calls
   * {@link android.support.v7.util.SortedList.Callback#onInserted(int, int)} only once with the proper item count. If an event
   * cannot be merged with the previous event, the previous event is dispatched
   * to the callback instantly.
   * <p>
   * After running your data updates, you <b>must</b> call {@link #endBatchedUpdates()}
   * which will dispatch any deferred data change event to the current callback.
   * <p>
   * A sample implementation may look like this:
   * <pre>
   *     mSortedList.beginBatchedUpdates();
   *     try {
   *         mSortedList.add(item1)
   *         mSortedList.add(item2)
   *         mSortedList.remove(item3)
   *         ...
   *     } finally {
   *         mSortedList.endBatchedUpdates();
   *     }
   * </pre>
   * <p>
   * Instead of using this method to batch calls, you can use a Callback that extends
   * {@link android.support.v7.util.SortedList.BatchedCallback}. In that case, you must make sure that you are manually calling
   * {@link android.support.v7.util.SortedList.BatchedCallback#dispatchLastEvent()} right after you complete your data changes.
   * Failing to do so may create data inconsistencies with the Callback.
   * <p>
   * If the current Callback in an instance of {@link android.support.v7.util.SortedList.BatchedCallback}, calling this method
   * has no effect.
   */
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

  /**
   * Ends the update transaction and dispatches any remaining event to the callback.
   */
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

  /**
   * Removes the provided item from the list and calls {@link android.support.v7.util.SortedList.Callback#onRemoved(int, int)}.
   *
   * @param item The item to be removed from the list.
   * @return True if item is removed, false if item cannot be found in the list.
   */
  public boolean remove(T item) {
    throwIfMerging();
    return remove(item, true);
  }

  /**
   * Removes the item at the given index and calls {@link android.support.v7.util.SortedList.Callback#onRemoved(int, int)}.
   *
   * @param index The index of the item to be removed.
   * @return The removed item.
   */
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

  /**
   * Updates the item at the given index and calls {@link android.support.v7.util.SortedList.Callback#onChanged(int, int)} and/or
   * {@link android.support.v7.util.SortedList.Callback#onMoved(int, int)} if necessary.
   * <p>
   * You can use this method if you need to change an existing Item such that its position in the
   * list may change.
   * <p>
   * If the new object is a different object (<code>get(index) != item</code>) and
   * {@link android.support.v7.util.SortedList.Callback#areContentsTheSame(Object, Object)} returns <code>true</code>, SortedList
   * avoids calling {@link android.support.v7.util.SortedList.Callback#onChanged(int, int)} otherwise it calls
   * {@link android.support.v7.util.SortedList.Callback#onChanged(int, int)}.
   * <p>
   * If the new position of the item is different than the provided <code>index</code>,
   * SortedList
   * calls {@link android.support.v7.util.SortedList.Callback#onMoved(int, int)}.
   *
   * @param index The index of the item to replace
   * @param item  The item to replace the item at the given Index.
   * @see #add(Object)
   */
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

  /**
   * This method can be used to recalculate the position of the item at the given index, without
   * triggering an {@link android.support.v7.util.SortedList.Callback#onChanged(int, int)} callback.
   * <p>
   * If you are editing objects in the list such that their position in the list may change but
   * you don't want to trigger an onChange animation, you can use this method to re-position it.
   * If the item changes position, SortedList will call {@link android.support.v7.util.SortedList.Callback#onMoved(int, int)}
   * without
   * calling {@link android.support.v7.util.SortedList.Callback#onChanged(int, int)}.
   * <p>
   * A sample usage may look like:
   *
   * <pre>
   *     final int position = mSortedList.indexOf(item);
   *     item.incrementPriority(); // assume items are sorted by priority
   *     mSortedList.recalculatePositionOfItemAt(position);
   * </pre>
   * In the example above, because the sorting criteria of the item has been changed,
   * mSortedList.indexOf(item) will not be able to find the item. This is why the code above
   * first
   * gets the position before editing the item, edits it and informs the SortedList that item
   * should be repositioned.
   *
   * @param index The current index of the Item whose position should be re-calculated.
   * @see #updateItemAt(int, Object)
   * @see #add(Object)
   */
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

  /**
   * Returns the item at the given index.
   *
   * @param index The index of the item to retrieve.
   * @return The item at the given index.
   * @throws java.lang.IndexOutOfBoundsException if provided index is negative or larger than the
   *                                             size of the list.
   */
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

  /**
   * Returns the position of the provided item.
   *
   * @param item The item to query for position.
   * @return The position of the provided item or {@link #INVALID_POSITION} if item is not in the
   * list.
   */
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