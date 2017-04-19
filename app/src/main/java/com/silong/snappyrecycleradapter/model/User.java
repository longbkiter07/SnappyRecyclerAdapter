package com.silong.snappyrecycleradapter.model;

import android.text.TextUtils;

/**
 * Created by SILONG on 8/28/16.
 */
public class User {

  public final String id;

  public final int age;

  public final Gender gender;

  public final String name;

  public User(String id, String name, int age, Gender gender) {
    this.id = id;
    this.age = age;
    this.gender = gender;
    this.name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof User) {
      return TextUtils.equals(((User) obj).id, id);
    } else {
      return super.equals(obj);
    }
  }

  public enum Gender {
    male,
    female
  }
}
