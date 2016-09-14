package com.silong.fastrecycler.callback;

import com.silong.fastrecycler.DataComparable;

import android.support.v7.util.DiffUtil;

import java.util.List;

import rx.Observable;

public class ObservableDiffCallback<D> extends DiffUtil.Callback {

  private final DataComparable<D> mDataComparable;

  private final List<D> mNewData;

  private final List<D> mOldData;

  ObservableDiffCallback(DataComparable<D> dataComparable, List<D> oldData, List<D> newData) {
    mOldData = oldData;
    mNewData = newData;
    mDataComparable = dataComparable;
  }

  public static <D> Observable<DiffUtil.DiffResult> calculate(DataComparable<D> dataComparable, List<D> oldData,
      List<D> newData) {
    return Observable.defer(() -> {
      DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ObservableDiffCallback<>(dataComparable, oldData, newData));
      return Observable.just(diffResult);
    });
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
