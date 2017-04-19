package me.silong.v2.observablerm;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by SILONG on 4/18/17.
 */

public class RxSortedList<T> {

  private static final int MIN_CAPACITY = 10;

  private static final Executor sExecutor = Executors.newSingleThreadExecutor();

  private final AsyncSortedList<T> mSortedList;

  private final RxSortedListCallback<T> mRxSortedListCallback;

  private List<T> mSyncList;

  public RxSortedList(Class<T> klass, RxSortedListCallback<T> callback) {
    this(klass, callback, MIN_CAPACITY);
  }

  public RxSortedList(Class<T> klass, RxSortedListCallback<T> rxSortedListCallback, int initialCapacity) {
    mSyncList = new ArrayList<T>(initialCapacity);
    mSortedList = new AsyncSortedList<T>(klass, new AsyncSortedListCallback(rxSortedListCallback), initialCapacity);
    mRxSortedListCallback = rxSortedListCallback;
  }

  public Observable<Void> clear() {
    return Observable.<Void>fromCallable(() -> {
      mSortedList.clear();
      return null;
    }).subscribeOn(Schedulers.from(sExecutor));
  }

  public Observable<Void> set(List<T> items) {
    return Observable.defer(() -> RxDiffCallback.calculate(mRxSortedListCallback, mSortedList.getData(), items)
        .subscribeOn(Schedulers.from(sExecutor))
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(diffResultListPair -> {
          mSyncList.clear();
          mSyncList.addAll(diffResultListPair.second);
          diffResultListPair.first.dispatchUpdatesTo(mRxSortedListCallback);
        })
        .map(diffResult -> null));
  }

  public Observable<Integer> add(T item) {
    return Observable.fromCallable(() -> mSortedList.add(item))
        .subscribeOn(Schedulers.from(sExecutor));
  }

  public Observable<Void> addAll(T[] items, boolean mayModifyInput) {
    return Observable.<Void>fromCallable(() -> {
      mSortedList.addAll(items, mayModifyInput);
      return null;
    }).subscribeOn(Schedulers.from(sExecutor));
  }

  public Observable<Void> addAll(T... items) {
    return Observable.<Void>fromCallable(() -> {
      mSortedList.addAll(items);
      return null;
    }).subscribeOn(Schedulers.from(sExecutor));
  }

  public Observable<Void> addAll(Collection<T> items) {
    return Observable.<Void>fromCallable(() -> {
      mSortedList.addAll(items);
      return null;
    }).subscribeOn(Schedulers.from(sExecutor));
  }

  public Observable<Void> updateItemAt(int index, T item) {
    return Observable.<Void>fromCallable(() -> {
      mSortedList.updateItemAt(index, item);
      return null;
    }).subscribeOn(Schedulers.from(sExecutor));
  }

  public Observable<Integer> indexOf(T item) {
    return Observable.fromCallable(() -> mSortedList.indexOf(item))
        .subscribeOn(Schedulers.from(sExecutor));
  }

  public T getItemAt(int index) throws IndexOutOfBoundsException {
    return mSyncList.get(index);
  }

  public int size() {
    return mSyncList.size();
  }

  private class AsyncSortedListCallback extends AsyncSortedList.Callback<T> {

    private final WeakReference<RxSortedListCallback<T>> mRxSortedListCallbackWeakReference;


    AsyncSortedListCallback(RxSortedListCallback<T> rxSortedListCallback) {
      mRxSortedListCallbackWeakReference = new WeakReference<RxSortedListCallback<T>>(rxSortedListCallback);
    }

    @Override
    public int compare(T o1, T o2) {
      RxSortedListCallback<T> rxSortedListCallback = mRxSortedListCallbackWeakReference.get();
      if (rxSortedListCallback != null) {
        return rxSortedListCallback.compare(o1, o2);
      }
      return 0;
    }

    @Override
    public void onInserted(int position, T[] ts) {
      Observable.fromCallable(() -> Arrays.asList(ts))
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(tList -> {
            mSyncList.addAll(tList);
            RxSortedListCallback<T> rxSortedListCallback = mRxSortedListCallbackWeakReference.get();
            if (rxSortedListCallback != null) {
              rxSortedListCallback.onInserted(position, ts.length);
            }
          });
    }

    @Override
    public void onRemoved(int position, int count) {
      Observable.just(null)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(objects -> {
            //remove range http://stackoverflow.com/questions/2289183/why-is-javas-abstractlists-removerange-method-protected
            mSyncList.subList(position, position + count).clear();
            RxSortedListCallback<T> rxSortedListCallback = mRxSortedListCallbackWeakReference.get();
            if (rxSortedListCallback != null) {
              rxSortedListCallback.onRemoved(position, count);
            }
          });
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
      Observable.just(null)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(objects -> {
            T t = mSyncList.remove(fromPosition);
            mSyncList.add(toPosition, t);
            RxSortedListCallback<T> rxSortedListCallback = mRxSortedListCallbackWeakReference.get();
            if (rxSortedListCallback != null) {
              rxSortedListCallback.onMoved(fromPosition, toPosition);
            }
          });
    }

    @Override
    public void onChanged(int position, T[] ts) {
      Observable.just(null)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(tList -> {
            for (int i = 0; i < ts.length; i++) {
              mSyncList.set(position + i, ts[i]);
            }
            RxSortedListCallback<T> rxSortedListCallback = mRxSortedListCallbackWeakReference.get();
            if (rxSortedListCallback != null) {
              rxSortedListCallback.onChanged(position, ts.length);
            }
          });
    }

    @Override
    public boolean areContentsTheSame(T oldItem, T newItem) {
      RxSortedListCallback<T> rxSortedListCallback = mRxSortedListCallbackWeakReference.get();
      if (rxSortedListCallback != null) {
        return rxSortedListCallback.areContentsTheSame(oldItem, newItem);
      }
      return false;
    }

    @Override
    public boolean areItemsTheSame(T item1, T item2) {
      RxSortedListCallback<T> rxSortedListCallback = mRxSortedListCallbackWeakReference.get();
      if (rxSortedListCallback != null) {
        return rxSortedListCallback.areItemsTheSame(item1, item2);
      }
      return false;
    }
  }
}
