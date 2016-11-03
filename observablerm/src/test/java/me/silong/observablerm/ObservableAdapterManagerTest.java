package me.silong.observablerm;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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

  @Test(timeout = 5000)
  public void testQueueingEvent() throws Exception {
    ObservableAdapterManager<TestData> observableAdapterManager = new ObservableAdapterManager<TestData>(null, new ArrayList<>(),
        new DataComparable<TestData>() {
          @Override
          public boolean areContentsTheSame(TestData oldData, TestData newData) {
            return oldData.name.equals(newData.name);
          }

          @Override
          public boolean areItemsTheSame(TestData oldData, TestData newData) {
            return oldData.id.equals(newData.id);
          }
        });
    PublishSubject<List<TestData>> subject = PublishSubject.create();
    TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
    subject
        .flatMap(testData -> observableAdapterManager.setItems(testData))
        .subscribe(testSubscriber);
    subject.onNext(generateTestData(100));
    Thread.sleep(5);
    subject.onNext(generateTestData(0));
    Thread.sleep(5);
    subject.onNext(generateTestData(101));
    Thread.sleep(5);
    subject.onNext(generateTestData(0));
    Thread.sleep(5);
    subject.onNext(generateTestData(102));
    Thread.sleep(5);
    subject.onNext(generateTestData(0));
    Thread.sleep(5);
    subject.onNext(generateTestData(200));
    Thread.sleep(5);
    subject.onNext(generateTestData(0));
    Thread.sleep(5);
    subject.onNext(generateTestData(201));
    subject.onNext(generateTestData(200));
    Thread.sleep(5);
    subject.onNext(generateTestData(0));
    Thread.sleep(5);
    subject.onNext(generateTestData(201));
    subject.onNext(generateTestData(200));
    Thread.sleep(5);
    subject.onNext(generateTestData(0));
    Thread.sleep(5);
    subject.onNext(generateTestData(201));
    Thread.sleep(2000);
    //    subject.onCompleted();
    //    testSubscriber.awaitTerminalEvent();
    assertThat(testSubscriber.getOnNextEvents().size(), equalTo(15));
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
