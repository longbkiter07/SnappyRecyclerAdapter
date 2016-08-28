package com.silong.fastrecycler;

import com.silong.fastrecycler.logs.Ln;

import android.support.v7.util.DiffUtil;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by SILONG on 8/27/16.
 */
public class Utils {

  private Utils() {

  }

  public static <D> Observable<DiffUtil.DiffResult> calculate(DataComparable<D> dataComparable, List<D> oldData,
      List<D> newData) {
    return Observable.create(subscriber -> {
      try {
        long time = System.currentTimeMillis();
        Ln.d("calculating");
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FastDiffCallback<>(dataComparable, oldData, newData));
        Ln.d("calculated:" + (System.currentTimeMillis() - time));
        onNext(subscriber, diffResult, true);
      } catch (Exception e) {
        onError(subscriber, e);
      }
    });
  }

  public static <T> void onError(Subscriber<T> subscriber, Throwable e) {
    if (subscriber != null && !subscriber.isUnsubscribed()) {
      subscriber.onError(e == null ? new Exception("unknown exception") : e);
    }
  }

  public static <T> void onNext(Subscriber<T> subscriber, T data, boolean isComplete) {
    if (subscriber != null && !subscriber.isUnsubscribed()) {
      subscriber.onNext(data);
      if (isComplete) {
        subscriber.onCompleted();
      }
    }
  }
}
