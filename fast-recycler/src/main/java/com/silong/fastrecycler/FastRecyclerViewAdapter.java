package com.silong.fastrecycler;

import com.silong.fastrecycler.rx.BehaviorBufferOperator;

import android.support.v7.widget.RecyclerView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by SILONG on 8/27/16.
 */
public abstract class FastRecyclerViewAdapter<D, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

  private static final int BUFFER_TIME = 25; //ms

  private static final String TAG = FastRecyclerViewAdapter.class.getSimpleName();

  private final RecyclerList<D> mRecyclerList;

  private final DataComparable<D> mDataComparable;

  private final PublishSubject<Behavior<D>> mProcessingSubject;

  private final PublishSubject<Behavior<D>> mFinishedSubject;

  public FastRecyclerViewAdapter(RecyclerList<D> recyclerList, DataComparable<D> dataComparable) {
    mRecyclerList = recyclerList;
    mDataComparable = dataComparable;
    mProcessingSubject = PublishSubject.create();
    mFinishedSubject = PublishSubject.create();
    mProcessingSubject
        .compose(BehaviorBufferOperator.applyBehaviorBuffer(BUFFER_TIME, TimeUnit.MILLISECONDS, Schedulers.computation()))
        .concatMap(behaviors -> Observable.from(behaviors).concatMap(behavior -> processBehaviors(behavior)))
        .subscribe();
  }

  private Observable<Void> processBehaviors(Behavior<D> behavior) {
    if (behavior.mAction == Action.SET) {
      return Utils.calculate(mDataComparable, mRecyclerList.newCurrentList(), behavior.mItems)
          .observeOn(AndroidSchedulers.mainThread())
          .doOnNext(diffResult -> {
            mRecyclerList.setItems(behavior.mItems);
            diffResult.dispatchUpdatesTo(this);
          })
          .<Void>map(diffResult -> null)
          .doOnNext(o -> mFinishedSubject.onNext(behavior));
    } else {
      return Observable.<Void>create(subscriber -> {
        switch (behavior.mAction) {
          case ADD:
            if (behavior.mPos >= 0) {
              mRecyclerList.addAll(behavior.mItems, behavior.mPos);
            } else {
              mRecyclerList.addAll(behavior.mItems);
            }
            break;
          case UPDATE:
            mRecyclerList.update(behavior.mItems.get(0), behavior.mPos);
            break;
          case REMOVE:
            if (behavior.mPos >= 0) {
              mRecyclerList.remove(behavior.mPos);
            } else {
              mRecyclerList.remove(behavior.mItems.get(0));
            }
            break;
          case CLEAR:
            mRecyclerList.clear();
            break;
        }
      }).doOnNext(o -> mFinishedSubject.onNext(behavior))
          .subscribeOn(AndroidSchedulers.mainThread());
    }
  }

  private Observable<Void> submitBehavior(Behavior<D> behavior) {
    return Observable.just(behavior)
        .doOnNext(dBehavior -> mProcessingSubject.onNext(dBehavior))
        .flatMap(dBehavior -> mFinishedSubject.filter(finishedBehavior -> dBehavior == finishedBehavior).take(1))
        .map(dBehavior -> null);
  }

  public Observable<Void> add(D item) {
    return submitBehavior(new Behavior<>(item, Action.ADD));
  }

  public Observable<Void> add(D item, int pos) {
    return submitBehavior(new Behavior<D>(item, pos, Action.ADD));
  }

  public Observable<Void> remove(int pos) {
    return submitBehavior(new Behavior<D>(Collections.emptyList(), pos, Action.REMOVE));
  }

  public Observable<Void> remove(D item) {
    return submitBehavior(new Behavior<D>(item, Action.REMOVE));
  }

  public Observable<Void> addAll(List<D> items, int startPos) {
    return submitBehavior(new Behavior<D>(items, startPos, Action.ADD));
  }

  public Observable<Void> addAll(List<D> items) {
    return submitBehavior(new Behavior<D>(items, Action.ADD));
  }

  public Observable<Void> clear() {
    return submitBehavior(new Behavior<D>(Collections.emptyList(), Action.CLEAR));
  }

  public Observable<Void> setItems(List<D> items) {
    if (mRecyclerList.size() == 0) {
      return Observable.<Void>create(subscriber -> {
        mRecyclerList.setItems(items);
        notifyDataSetChanged();
        Utils.onNext(subscriber, null, true);
      }).subscribeOn(AndroidSchedulers.mainThread());
    } else {
      return submitBehavior(new Behavior<D>(items, Action.SET));
    }
  }

  public Observable<Void> update(int position, D item) {
    return submitBehavior(new Behavior<D>(item, position, Action.UPDATE));
  }

  public D getItemAt(int pos) {
    return mRecyclerList.getItemAt(pos);
  }

  @Override
  public int getItemCount() {
    return mRecyclerList.size();
  }

  private enum Action {
    ADD,
    REMOVE,
    CLEAR,
    SET,
    UPDATE
  }

  private static class Behavior<D> {

    List<D> mItems;

    int mPos;

    Action mAction;

    public Behavior(D item, int pos, Action action) {
      mItems = Arrays.asList(item);
      mPos = pos;
      mAction = action;
    }

    public Behavior(List<D> items, Action action) {
      this(items, -1, action);
    }

    public Behavior(List<D> items, int pos, Action action) {
      mItems = items;
      mPos = pos;
      mAction = action;
    }

    public Behavior(D item, Action action) {
      this(item, -1, action);
    }
  }
}
