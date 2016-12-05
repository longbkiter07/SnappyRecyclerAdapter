package me.silong.observablerm;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.silong.observablerm.callback.ObservableDiffCallback;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

/**
 * Created by SILONG on 9/14/16.
 */
public class ObservableAdapterManager<D> {

  private static final int MAX_SIZE_TO_CALL_DIFF = 512; //ms

  private final List<D> mItems;

  private final DataComparable<D> mDataComparable;

  private final PublishSubject<Behavior<D>> mProcessingSubject;

  private final BehaviorSubject<Behavior<D>> mFinishedSubject;

  @Nullable private RecyclerView.Adapter mAdapter;

  private Subscription mProcessingSubscription;

  public ObservableAdapterManager(@Nullable RecyclerView.Adapter adapter, List<D> items, DataComparable<D> dataComparable) {
    mItems = items;
    mDataComparable = dataComparable;
    mProcessingSubject = PublishSubject.create();
    mFinishedSubject = BehaviorSubject.create();
    mAdapter = adapter;
    init();
  }

  public ObservableAdapterManager(List<D> items, DataComparable<D> dataComparable) {
    this(null, items, dataComparable);
  }

  private void unsubscribe() {
    if (mProcessingSubscription != null && !mProcessingSubscription.isUnsubscribed()) {
      mProcessingSubscription.unsubscribe();
    }
  }

  public void clearEvents() {
    init();
  }

  private void init() {
    unsubscribe();
    mProcessingSubscription = mProcessingSubject
        .onBackpressureBuffer()
        .observeOn(Schedulers.computation())
        .concatMap(dBehavior -> processBehaviors(dBehavior).doOnNext(aVoid1 -> {
          mFinishedSubject.onNext(dBehavior);
        }))
        .subscribe();
  }

  public void attachTo(RecyclerView.Adapter adapter) {
    mAdapter = adapter;
  }

  private Observable<Void> processSetWithDiffCallback(Behavior<D> behavior) {
    return ObservableDiffCallback.calculate(mDataComparable, new ArrayList<>(mItems), behavior.mItems)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(diffResult -> {
          mItems.clear();
          mItems.addAll(behavior.mItems);
          if (mAdapter != null) {
            diffResult.dispatchUpdatesTo(mAdapter);
          }
        })
        .<Void>map(diffResult -> null);
  }

  private Observable<Void> processSetWithNotify(Behavior<D> behavior) {
    return Observable.defer(() -> {
      mItems.clear();
      mItems.addAll(behavior.mItems);
      if (mAdapter != null) {
        mAdapter.notifyDataSetChanged();
      }
      return Observable.<Void>just(null);
    })
        .subscribeOn(AndroidSchedulers.mainThread());
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
          if (mAdapter != null) {
            mAdapter.notifyItemRangeInserted(startPos, size);
          }
          break;
        case UPDATE:
          D item = behavior.mItems.get(0);
          D oldItem = mItems.get(behavior.mPos);
          mItems.set(behavior.mPos, item);
          if (mAdapter != null && shouldCallUpdate(oldItem, item)) {
            mAdapter.notifyItemChanged(behavior.mPos);
          }
          break;
        case REMOVE:
          int removeIndex = -1;
          if (behavior.mPos >= 0) {
            removeIndex = behavior.mPos;
          } else {
            if (mDataComparable != null) {
              D removingItem = behavior.mItems.get(0);
              for (int i = 0; i < mItems.size(); i++) {
                item = mItems.get(i);
                if (mDataComparable.areItemsTheSame(removingItem, item)) {
                  removeIndex = i;
                  break;
                }
              }
            } else {
              removeIndex = mItems.indexOf(behavior.mItems.get(0));
            }
          }
          mItems.remove(removeIndex);
          if (mAdapter != null) {
            mAdapter.notifyItemRemoved(removeIndex);
          }
          break;
        case MOVE:
          oldItem = mItems.remove(behavior.mPos);
          item = behavior.mItems.get(0);
          mItems.add(behavior.mDestPos, item);
          if (mAdapter != null) {
            mAdapter.notifyItemMoved(behavior.mPos, behavior.mDestPos);
            if (mDataComparable != null && shouldCallUpdate(oldItem, item)) {
              mAdapter.notifyItemChanged(behavior.mDestPos);
            }
          }
          break;
        case CLEAR:
          size = mItems.size();
          mItems.clear();
          if (mAdapter != null) {
            mAdapter.notifyItemRangeRemoved(0, size);
          }
          break;
      }
      return Observable.<Void>just(null);
    }).subscribeOn(AndroidSchedulers.mainThread());
  }

  private boolean shouldCallUpdate(D oldItem, D newItem) {
    return mDataComparable != null
        && (!mDataComparable.areItemsTheSame(oldItem, newItem)
        || !mDataComparable.areContentsTheSame(oldItem, newItem));
  }

  private Observable<Void> processBehaviors(Behavior<D> behavior) {
    return Observable.defer(() -> {
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
    });
  }

  private Observable<Void> submitBehavior(Behavior<D> behavior) {
    return mFinishedSubject.filter(dBehavior -> dBehavior == behavior)
        .take(1)
        .<Void>map(aLong -> null)
        .doOnSubscribe(() -> mProcessingSubject.onNext(behavior));
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

  public Observable<Void> addAll(List<? extends D> items, int startPos) {
    return submitBehavior(new Behavior<D>(items, startPos, Action.ADD));
  }

  public Observable<Void> addAll(List<? extends D> items) {
    return submitBehavior(new Behavior<D>(items, Action.ADD));
  }

  public Observable<Void> clear() {
    return submitBehavior(new Behavior<D>(Collections.emptyList(), Action.CLEAR));
  }

  public Observable<Void> move(D item, int startPos, int destPost) {
    return submitBehavior(new Behavior<D>(item, startPos, Action.MOVE, destPost));
  }

  public Observable<Void> setItems(List<? extends D> items) {
    return submitBehavior(new Behavior<D>(items, Action.SET));
  }

  public Observable<Void> update(D item, int position) {
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
    MOVE,
    UPDATE
  }

  private static class Behavior<D> {

    final List<? extends D> mItems;

    final int mPos;

    final Action mAction;

    final int mDestPos;

    public Behavior(D item, int pos, Action action) {
      this(Arrays.asList(item), pos, action, pos);
    }

    public Behavior(List<? extends D> items, Action action) {
      this(items, -1, action);
    }

    public Behavior(List<? extends D> items, int pos, Action action) {
      this(items, pos, action, pos);
    }

    public Behavior(D item, int pos, Action action, int destPos) {
      this(Arrays.asList(item), pos, action, destPos);
    }

    public Behavior(List<? extends D> items, int pos, Action action, int destPos) {
      mItems = items;
      mPos = pos;
      mAction = action;
      mDestPos = destPos;
    }

    public Behavior(D item, Action action) {
      this(item, -1, action);
    }
  }
}
