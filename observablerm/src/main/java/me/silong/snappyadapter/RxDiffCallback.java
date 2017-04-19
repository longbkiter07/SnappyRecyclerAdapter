package me.silong.snappyadapter;

import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;

import java.util.List;

import rx.Observable;

class RxDiffCallback<D> extends DiffUtil.Callback {

  private final RxSortedListCallback<D> mDataComparable;

  private final List<D> mNewData;

  private final List<D> mOldData;

  RxDiffCallback(RxSortedListCallback<D> dataComparable, List<D> oldData, List<D> newData) {
    mOldData = oldData;
    mNewData = newData;
    mDataComparable = dataComparable;
  }

  public static <D> Observable<Pair<DiffUtil.DiffResult, List<D>>> calculate(RxSortedListCallback<D> dataComparable,
      List<D> oldData,
      List<D> newData) {
    return Observable.defer(() -> {
      DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new RxDiffCallback<>(dataComparable, oldData, newData));
      return Observable.just(new Pair<>(diffResult, newData));
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
