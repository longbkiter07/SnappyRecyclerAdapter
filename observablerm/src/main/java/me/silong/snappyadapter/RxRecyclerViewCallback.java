package me.silong.snappyadapter;

import android.support.v7.widget.RecyclerView;

/**
 * Created by SILONG on 4/24/17.
 */

public abstract class RxRecyclerViewCallback<T> extends RxSortedListCallback<T> {

  private final RecyclerView.Adapter mAdapter;

  public RxRecyclerViewCallback(RecyclerView.Adapter adapter) {
    mAdapter = adapter;
  }

  @Override
  public void onChanged(int position, int count) {
    mAdapter.notifyItemChanged(position, count);
  }

  @Override
  public void onInserted(int position, int count) {
    mAdapter.notifyItemRangeInserted(position, count);
  }

  @Override
  public void onRemoved(int position, int count) {
    mAdapter.notifyItemRangeRemoved(position, count);
  }

  @Override
  public void onMoved(int fromPosition, int toPosition) {
    mAdapter.notifyItemMoved(fromPosition, toPosition);
  }
}
