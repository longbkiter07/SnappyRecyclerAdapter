package me.silong.observablerm;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.silong.observablerm.callback.ObservableDiffCallback;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by SILONG on 9/14/16.
 */
public class ObservableAdapterManager<D> {

  private static final int MAX_SIZE_TO_CALL_DIFF = 512; //ms

  private final List<D> mItems;

  private final DataComparable<D> mDataComparable;

  private final PublishSubject<Behavior<D>> mProcessingSubject;

  private final PublishSubject<Behavior<D>> mFinishedSubject;

  private final RecyclerView.Adapter mAdapter;

  public ObservableAdapterManager(RecyclerView.Adapter adapter, List<D> items, DataComparable<D> dataComparable) {
    mItems = items;
    mDataComparable = dataComparable;
    mProcessingSubject = PublishSubject.create();
    mFinishedSubject = PublishSubject.create();
    mAdapter = adapter;
    mProcessingSubject
        .observeOn(Schedulers.computation())
        .concatMap(behavior -> processBehaviors(behavior))
        .subscribe();
  }

  private Observable<Void> processSetWithDiffCallback(Behavior<D> behavior) {
    return ObservableDiffCallback.calculate(mDataComparable, new ArrayList<D>(mItems), behavior.mItems)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(diffResult -> {
          mItems.clear();
          mItems.addAll(behavior.mItems);
          diffResult.dispatchUpdatesTo(mAdapter);
        })
        .<Void>map(diffResult -> null)
        .doOnNext(o -> mFinishedSubject.onNext(behavior));
  }

  private Observable<Void> processSetWithNotify(Behavior<D> behavior) {
    return Observable.defer(() -> {
      mItems.clear();
      mItems.addAll(behavior.mItems);
      mAdapter.notifyDataSetChanged();
      return Observable.<Void>just(null);
    })
        .subscribeOn(AndroidSchedulers.mainThread())
        .doOnNext(o -> mFinishedSubject.onNext(behavior));
  }

  private Observable<Void> processSingleOperator(Behavior<D> behavior) {
    return Observable.defer(() -> {
      switch (behavior.mAction) {
        case ADD:
          int size = behavior.mItems.size();
          int startPos;
          if (behavior.mPos >= 0) {
            mItems.addAll(behavior.mPos, behavior.mItems);
            startPos = behavior.mPos;
          } else {
            startPos = mItems.size();
            mItems.addAll(behavior.mItems);
          }
          mAdapter.notifyItemRangeInserted(startPos, size);
          break;
        case UPDATE:
          mItems.set(behavior.mPos, behavior.mItems.get(0));
          mAdapter.notifyItemChanged(behavior.mPos);
          break;
        case REMOVE:
          int removeIndex;
          if (behavior.mPos >= 0) {
            removeIndex = behavior.mPos;
          } else {
            removeIndex = mItems.indexOf(behavior.mItems.get(0));
          }
          mItems.remove(removeIndex);
          mAdapter.notifyItemRemoved(removeIndex);
          break;
        case CLEAR:
          size = mItems.size();
          mItems.clear();
          mAdapter.notifyItemRangeRemoved(0, size);
          break;
      }
      return Observable.<Void>just(null);
    }).doOnNext(o -> mFinishedSubject.onNext(behavior))
        .subscribeOn(AndroidSchedulers.mainThread());
  }

  private Observable<Void> processBehaviors(Behavior<D> behavior) {
    if (behavior.mAction == Action.SET) {
      if (mDataComparable != null && mItems.size() <= MAX_SIZE_TO_CALL_DIFF
          && behavior.mItems.size() <= MAX_SIZE_TO_CALL_DIFF) {
        return processSetWithDiffCallback(behavior);
      } else {
        return processSetWithNotify(behavior);
      }
    } else {
      return processSingleOperator(behavior);
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
    if (mItems.size() == 0) {
      return Observable.defer(() -> {
        mItems.clear();
        mItems.addAll(items);
        mAdapter.notifyDataSetChanged();
        return Observable.<Void>just(null);
      }).subscribeOn(AndroidSchedulers.mainThread());
    } else {
      return submitBehavior(new Behavior<D>(items, Action.SET));
    }
  }

  public Observable<Void> update(int position, D item) {
    return submitBehavior(new Behavior<D>(item, position, Action.UPDATE));
  }

  public D getItemAt(int pos) {
    return mItems.get(pos);
  }

  public int getItemCount() {
    return mItems.size();
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
