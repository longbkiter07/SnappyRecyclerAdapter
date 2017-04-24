package me.silong.snappyadapter;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by SILONG on 4/21/17.
 */

public class RxSortedList<T> {

  private static Executor sExecutor = Executors.newSingleThreadExecutor();

  private SnappySortedList<T> mSnappySortedList;

  public RxSortedList(Class<T> klass, RxSortedListCallback callback) {
    mSnappySortedList = new SnappySortedList<T>(klass, callback);
  }

  public RxSortedList(Class klass, RxSortedListCallback callback, int initialCapacity) {
    mSnappySortedList = new SnappySortedList<T>(klass, callback);
  }

  private <D> Observable.Transformer<D, D> queueEvent() {
    return dObservable -> Observable.just(null)
        .subscribeOn(Schedulers.from(sExecutor))
        .observeOn(AndroidSchedulers.mainThread())
        .flatMap(o -> dObservable);
  }

  public int size() {
    return mSnappySortedList.size();
  }

  public Observable<Integer> add(T item) {
    return Observable.fromCallable(() -> mSnappySortedList.add(item))
        .compose(queueEvent());
  }

  public Observable<Void> addAll(T[] items, boolean mayModifyInput) {
    return Observable.<Void>fromCallable(() -> {
      mSnappySortedList.addAll(items, mayModifyInput);
      return null;
    })
        .compose(queueEvent());
  }

  public Observable<Void> addAll(T[] items) {
    return Observable.<Void>fromCallable(() -> {
      mSnappySortedList.addAll(items);
      return null;
    })
        .compose(queueEvent());
  }

  public Observable<Void> addAll(Collection<T> items) {
    return Observable.<Void>fromCallable(() -> {
      mSnappySortedList.addAll(items);
      return null;
    }).compose(queueEvent());
  }

  public void beginBatchedUpdates() {
    mSnappySortedList.beginBatchedUpdates();
  }

  public void endBatchedUpdates() {
    mSnappySortedList.endBatchedUpdates();
  }

  public Observable<Boolean> remove(T item) {
    return Observable.fromCallable(() -> mSnappySortedList.remove(item)).compose(queueEvent());
  }

  public Observable<T> removeItemAt(int index) {
    return Observable.fromCallable(() -> mSnappySortedList.removeItemAt(index)).compose(queueEvent());
  }

  public Observable<Void> updateItemAt(int index, T item) {
    return Observable.<Void>fromCallable(() -> {
      mSnappySortedList.updateItemAt(index, item);
      return null;
    }).compose(queueEvent());
  }


  public Observable<Void> recalculatePositionOfItemAt(int index) {
    return Observable.<Void>fromCallable(() -> {
      mSnappySortedList.recalculatePositionOfItemAt(index);
      return null;
    }).compose(queueEvent());
  }


  public T get(int index) throws IndexOutOfBoundsException {
    return mSnappySortedList.get(index);
  }

  public Observable<Integer> indexOf(T item) {
    return Observable.fromCallable(() -> mSnappySortedList.indexOf(item)).compose(queueEvent());
  }

  public Observable<Void> clear() {
    return Observable.<Void>fromCallable(() -> {
      mSnappySortedList.clear();
      return null;
    }).compose(queueEvent());
  }

  public Observable<Void> set(Collection<T> items, boolean isSorted) {
    return mSnappySortedList.set(items, isSorted)
        .subscribeOn(Schedulers.from(sExecutor));
  }

  public Observable<Void> set(Collection<T> items) {
    return mSnappySortedList.set(items)
        .subscribeOn(Schedulers.from(sExecutor));
  }
}
