package me.silong.observablerm;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.schedulers.Schedulers;

/**
 * Created by SILONG on 11/3/16.
 */
public class ObservableAdapterManagerTest {

  private static List<TestData> generateTestData(int count) {
    List<TestData> testDatas = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      testDatas.add(new TestData("id" + i, "name" + i));
    }
    return testDatas;
  }

  @Before
  public void setup() {
    RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
      @Override
      public Scheduler getMainThreadScheduler() {
        return Schedulers.from(Executors.newSingleThreadExecutor());
      }
    });
  }

  @Test
  public void testScheduler() throws Exception {
    Scheduler scheduler = Schedulers.newThread();
    Subscription subscription = Observable.interval(100, TimeUnit.MILLISECONDS, scheduler)
        .doOnNext(aLong -> System.out.println("onNext 1:" + aLong))
        .map(aLong -> {
          for (int i = 0; i < 100000000; i++) {
            aLong += i;
            aLong ^= i;
          }
          return aLong;
        })
        .doOnNext(aLong -> System.out.println("onNext 2:" + aLong + "-" + System.currentTimeMillis() / 1000))
        .subscribeOn(scheduler)
        .doOnTerminate(() -> System.out.println("terminated"))
        .doOnUnsubscribe(() -> System.out.println("unsubscribed-" + System.currentTimeMillis() / 1000))
        .doOnCompleted(() -> System.out.println("completed-" + System.currentTimeMillis() / 1000))
        .subscribe(new Subscriber<Long>() {
          @Override
          public void onCompleted() {

          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(Long aLong) {
            if (!isUnsubscribed()) {
              System.out.println("onNext event 1:" + aLong);
            }
            System.out.println("onNext event 2:" + aLong);
          }
        });
    Thread.sleep(3200);
    subscription.unsubscribe();
    Thread.sleep(2000);
  }

  @Test(timeout = 5000)
  public void testQueueingEvent() throws Exception {
    //    ObservableAdapterManager<TestData> observableAdapterManager = new ObservableAdapterManager<TestData>(null, new ArrayList<>(),
    //        new DataComparable<TestData>() {
    //          @Override
    //          public boolean areContentsTheSame(TestData oldData, TestData newData) {
    //            return oldData.name.equals(newData.name);
    //          }
    //
    //          @Override
    //          public boolean areItemsTheSame(TestData oldData, TestData newData) {
    //            return oldData.id.equals(newData.id);
    //          }
    //        });
    //    PublishSubject<List<TestData>> subject = PublishSubject.create();
    //    TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
    //    subject
    //        .flatMap(testData -> observableAdapterManager.setItems(testData))
    //        .subscribe(testSubscriber);
    //    subject.onNext(generateTestData(100));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(0));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(101));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(0));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(102));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(0));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(200));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(0));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(201));
    //    subject.onNext(generateTestData(200));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(0));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(201));
    //    subject.onNext(generateTestData(200));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(0));
    //    Thread.sleep(5);
    //    subject.onNext(generateTestData(201));
    //    Thread.sleep(2000);
    //    //    subject.onCompleted();
    //    //    testSubscriber.awaitTerminalEvent();
    //    assertThat(testSubscriber.getOnNextEvents().size(), equalTo(15));
  }

  @Test(timeout = 5000)
  public void testClearData() throws Exception {
    //    ObservableAdapterManager<TestData> observableAdapterManager = new ObservableAdapterManager<TestData>(null, new ArrayList<>(),
    //        new DataComparable<TestData>() {
    //          @Override
    //          public boolean areContentsTheSame(TestData oldData, TestData newData) {
    //            return oldData.name.equals(newData.name);
    //          }
    //
    //          @Override
    //          public boolean areItemsTheSame(TestData oldData, TestData newData) {
    //            return oldData.id.equals(newData.id);
    //          }
    //        });
    //    PublishSubject<List<TestData>> subject = PublishSubject.create();
    //    TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
    //    subject
    //        .flatMap(testData -> observableAdapterManager.setItems(testData))
    //        .doOnNext(aVoid -> System.out.println("onNext event"))
    //        .doOnUnsubscribe(() -> System.out.println("onUnsubscribed"))
    //        .subscribe(testSubscriber);
    //    subject.onNext(generateTestData(100));
    //    subject.onNext(generateTestData(0));
    //    subject.onNext(generateTestData(101));
    //    subject.onNext(generateTestData(0));
    //    subject.onNext(generateTestData(102));
    //    subject.onNext(generateTestData(0));
    //    subject.onNext(generateTestData(200));
    //    subject.onNext(generateTestData(0));
    //    subject.onNext(generateTestData(201));
    //    subject.onNext(generateTestData(200));
    //    subject.onNext(generateTestData(0));
    //    subject.onNext(generateTestData(201));
    //    subject.onNext(generateTestData(200));
    //    subject.onNext(generateTestData(0));
    //    subject.onNext(generateTestData(201));
    //    Thread.sleep(15);
    //    observableAdapterManager.clearEvents();
    //    Thread.sleep(2000);
    //    System.out.println("event count:" + testSubscriber.getValueCount());
  }

  private static class TestData {

    String id;

    String name;

    public TestData(String id, String name) {
      this.id = id;
      this.name = name;
    }
  }
}
