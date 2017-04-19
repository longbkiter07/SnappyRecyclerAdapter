package com.silong.snappyrecycleradapter.adapter;

import java.util.List;

/**
 * Created by SILONG on 4/19/17.
 */

public interface SyncList<T> {

  void add(T t);

  void add(List<T> users);

  void set(T t, int pos);

  void remove(T user);

  void remove(int index);

  void set(List<T> users);

  void clear();

  int getItemCount();
}
