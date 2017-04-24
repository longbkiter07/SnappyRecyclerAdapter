package me.silong.snappyadapter;

import android.support.v7.util.BatchingListUpdateCallback;
import android.support.v7.util.ListUpdateCallback;

import java.util.Comparator;

/**
 * The class that controls the behavior of the {@link android.support.v7.util.SortedList}.
 * <p>
 * It defines how items should be sorted and how duplicates should be handled.
 * <p>
 * SortedList calls the callback methods on this class to notify changes about the underlying
 * data.
 */
public abstract class RxSortedListCallback<T2> implements Comparator<T2>, ListUpdateCallback {

  /**
   * Similar to {@link java.util.Comparator#compare(Object, Object)}, should compare two and
   * return how they should be ordered.
   *
   * @param o1 The first object to compare.
   * @param o2 The second object to compare.
   * @return a negative integer, zero, or a positive integer as the
   * first argument is less than, equal to, or greater than the
   * second.
   */
  @Override
  abstract public int compare(T2 o1, T2 o2);

  /**
   * Called by the SortedList when the item at the given position is updated.
   *
   * @param position The position of the item which has been updated.
   * @param count    The number of items which has changed.
   */
  abstract public void onChanged(int position, int count);

  @Override
  public void onChanged(int position, int count, Object payload) {
    onChanged(position, count);
  }

  /**
   * Called by the SortedList when it wants to check whether two items have the same data
   * or not. SortedList uses this information to decide whether it should call
   * {@link #onChanged(int, int)} or not.
   * <p>
   * SortedList uses this method to check equality instead of {@link Object#equals(Object)}
   * so
   * that you can change its behavior depending on your UI.
   * <p>
   * For example, if you are using SortedList with a {@link android.support.v7.widget.RecyclerView.Adapter
   * RecyclerView.Adapter}, you should
   * return whether the items' visual representations are the same or not.
   *
   * @param oldItem The previous representation of the object.
   * @param newItem The new object that replaces the previous one.
   * @return True if the contents of the items are the same or false if they are different.
   */
  abstract public boolean areContentsTheSame(T2 oldItem, T2 newItem);

  /**
   * Called by the SortedList to decide whether two object represent the same Item or not.
   * <p>
   * For example, if your items have unique ids, this method should check their equality.
   *
   * @param item1 The first item to check.
   * @param item2 The second item to check.
   * @return True if the two items represent the same object or false if they are different.
   */
  abstract public boolean areItemsTheSame(T2 item1, T2 item2);

  /**
   * A callback implementation that can batch notify events dispatched by the SortedList.
   * <p>
   * This class can be useful if you want to do multiple operations on a SortedList but don't
   * want to dispatch each event one by one, which may result in a performance issue.
   * <p>
   * For example, if you are going to add multiple items to a SortedList, BatchedCallback call
   * convert individual <code>onInserted(index, 1)</code> calls into one
   * <code>onInserted(index, N)</code> if items are added into consecutive indices. This change
   * can help RecyclerView resolve changes much more easily.
   * <p>
   * If consecutive changes in the SortedList are not suitable for batching, BatchingCallback
   * dispatches them as soon as such case is detected. After your edits on the SortedList is
   * complete, you <b>must</b> always call {@link android.support.v7.util.SortedList.BatchedCallback#dispatchLastEvent()} to flush
   * all changes to the Callback.
   */
  public static class BatchedCallback<T2> extends RxSortedListCallback<T2> {

    final RxSortedListCallback<T2> mWrappedCallback;

    final BatchingListUpdateCallback mBatchingListUpdateCallback;

    /**
     * Creates a new BatchedCallback that wraps the provided Callback.
     *
     * @param wrappedCallback The Callback which should received the data change callbacks.
     *                        Other method calls (e.g. {@link #compare(Object, Object)} from
     *                        the SortedList are directly forwarded to this Callback.
     */
    public BatchedCallback(RxSortedListCallback<T2> wrappedCallback) {
      mWrappedCallback = wrappedCallback;
      mBatchingListUpdateCallback = new BatchingListUpdateCallback(mWrappedCallback);
    }

    @Override
    public int compare(T2 o1, T2 o2) {
      return mWrappedCallback.compare(o1, o2);
    }

    @Override
    public void onInserted(int position, int count) {
      mBatchingListUpdateCallback.onInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
      mBatchingListUpdateCallback.onRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
      mBatchingListUpdateCallback.onInserted(fromPosition, toPosition);
    }

    @Override
    public void onChanged(int position, int count) {
      mBatchingListUpdateCallback.onChanged(position, count, null);
    }

    @Override
    public boolean areContentsTheSame(T2 oldItem, T2 newItem) {
      return mWrappedCallback.areContentsTheSame(oldItem, newItem);
    }

    @Override
    public boolean areItemsTheSame(T2 item1, T2 item2) {
      return mWrappedCallback.areItemsTheSame(item1, item2);
    }

    /**
     * This method dispatches any pending event notifications to the wrapped Callback.
     * You <b>must</b> always call this method after you are done with editing the SortedList.
     */
    public void dispatchLastEvent() {
      mBatchingListUpdateCallback.dispatchLastEvent();
    }
  }
}