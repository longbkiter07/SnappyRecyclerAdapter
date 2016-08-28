package com.silong.fastrecycler;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class FastDiffCallback<D> extends DiffUtil.Callback {

  private final DataComparable<D> mDataComparable;

  private final List<D> mNewData;

  private final List<D> mOldData;

  FastDiffCallback(DataComparable<D> dataComparable, List<D> oldData, List<D> newData) {
    mOldData = oldData;
    mNewData = newData;
    mDataComparable = dataComparable;
  }

  @Override
  public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
    return mDataComparable.areContentsTheSame(mOldData.get(oldItemPosition), mNewData.get(newItemPosition));
  }

  @Override
  public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
    return mDataComparable.areItemsTheSame(mOldData.get(oldItemPosition), mNewData.get(newItemPosition));
  }

  @Override
  public int getNewListSize() {
    return mNewData.size();
  }

  @Override
  public int getOldListSize() {
    return mOldData.size();
  }
}