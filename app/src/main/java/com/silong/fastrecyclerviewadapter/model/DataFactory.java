package com.silong.fastrecyclerviewadapter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SILONG on 8/28/16.
 */
public class DataFactory {

  public static final int CHUNK = 1000;

  private DataFactory() {

  }

  public static List<User> fakeUsersToSet(int size) {
    List<User> users = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      users.add(new User("User_" + i, (int) (Math.random() * 30 + 20),
          Math.random() < 0.5 ? User.Gender.male : User.Gender.female));
    }
    return users;
  }

  public static List<User> fakeUsersToAddOrUpdate(int begin, int size) {
    List<User> users = new ArrayList<>(size);
    for (int i = begin; i < begin + size; i++) {
      users.add(new User("User_" + i, (int) (Math.random() * 30 + 20),
          Math.random() < 0.5 ? User.Gender.male : User.Gender.female));
    }
    return users;
  }

}
