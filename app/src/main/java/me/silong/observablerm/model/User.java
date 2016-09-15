package me.silong.observablerm.model;

import android.text.TextUtils;

/**
 * Created by SILONG on 8/28/16.
 */
public class User {

  public final String name;

  public final int age;

  public final Gender gender;

  public User(String name, int age, Gender gender) {
    this.name = name;
    this.age = age;
    this.gender = gender;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof User) {
      return TextUtils.equals(((User) obj).name, name);
    } else {
      return super.equals(obj);
    }
  }

  public enum Gender {
    male,
    female
  }
}
