package com.silong.fastrecycler.rx;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.observers.SerializedSubscriber;

public class BehaviorBufferOperator<T> implements Observable.Operator<List<T>, T> {

  private Scheduler mScheduler;

  private long mTime;

  private TimeUnit mTimeUnit;

  public BehaviorBufferOperator(long time, TimeUnit timeUnit, Scheduler scheduler) {
    mTime = time;
    mTimeUnit = timeUnit;
    mScheduler = scheduler;
  }

  public static <T> Observable.Transformer<T, List<T>> applyBehaviorBuffer(long time, TimeUnit timeUnit, Scheduler scheduler) {
    return tObservable -> tObservable.lift(new BehaviorBufferOperator<>(time, timeUnit, scheduler));
  }

  @Override
  public Subscriber<? super T> call(Subscriber<? super List<T>> subscriber) {
    final Scheduler.Worker inner = mScheduler.createWorker();
    SerializedSubscriber<List<T>> serialized = new SerializedSubscriber<List<T>>(subscriber);
    SingleBufferSubscriber<T> singleBufferSubscriber = new SingleBufferSubscriber<>(serialized, inner, mTime, mTimeUnit);
    subscriber.add(singleBufferSubscriber);
    return singleBufferSubscriber;
  }

  private static class SingleBufferSubscriber<T> extends Subscriber<T> {

    final Subscriber<? super List<T>> mChild;


    final Scheduler.Worker mInner;

    final long mTime;

    final TimeUnit mTimeUnit;

    /** Guarded by this. */
    List<T> mChunk;

    ListIterator<T> mChunkIterator;

    /** Guarded by this. */
    boolean mDone;

    public SingleBufferSubscriber(Subscriber<? super List<T>> child, Scheduler.Worker inner, long time, TimeUnit timeUnit) {
      mChild = child;
      mInner = inner;
      mChunk = new LinkedList<>();
      mChunkIterator = mChunk.listIterator();
      mTime = time;
      mTimeUnit = timeUnit;
      mDone = false;
    }

    @Override
    public void onCompleted() {
      onTimeOut();
      synchronized (this) {
        mDone = true;
      }
      mChild.onCompleted();
      unsubscribe();
    }

    @Override
    public void onError(Throwable e) {
      synchronized (this) {
        mDone = true;
      }
      mChild.onError(e);
      unsubscribe();
    }

    @Override
    public void onNext(T t) {
      synchronized (this) {
        if (mChunk.isEmpty()) {
          mInner.schedule(() -> onTimeOut(), mTime, mTimeUnit);
        }
        mChunkIterator.add(t);
      }
    }

    private void onTimeOut() {
      synchronized (this) {
        if (!mChunk.isEmpty() && !mDone) {
          mChild.onNext(mChunk);
          this.mChunk = new LinkedList<>();
          this.mChunkIterator = mChunk.listIterator();
        }
      }
    }
  }
}
