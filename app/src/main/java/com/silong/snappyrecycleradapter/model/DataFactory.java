package com.silong.snappyrecycleradapter.model;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by SILONG on 8/28/16.
 */
public class DataFactory {

  public static final int CHUNK = 10;

  private DataFactory() {

  }

  public static Observable<List<User>> fakeUsersToSet(int size) {
    return Observable.defer(() -> {
      List<User> users = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        int rand = (int) (Math.random() * 100000);
        users.add(new User("User_" + i, "User_" + rand, (int) (Math.random() * 30 + 20),
            Math.random() < 0.5 ? User.Gender.male : User.Gender.female));
      }
      return Observable.just(users);
    })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread());
  }

  public static Observable<List<User>> fakeUsersToAddOrUpdate(int begin, int size) {
    return Observable.defer(() -> {
      List<User> users = new ArrayList<>(size);
      for (int i = begin; i < begin + size; i++) {
        int rand = (int) (Math.random() * 10000);
        users.add(new User("User_" + i, "User_" + rand, (int) (Math.random() * 30 + 20),
            Math.random() < 0.5 ? User.Gender.male : User.Gender.female));
      }
      return Observable.just(users);
    }).subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread());
  }

}
