package com.silong.fastrecycler;

import java.util.List;

/**
 * Created by SILONG on 8/27/16.
 */
public interface RecyclerList<D> {

  D getItemAt(int pos);

  void add(D item);

  void add(D item, int pos);

  void remove(int pos);

  void remove(D item);

  void addAll(List<D> items, int startPos);

  void addAll(List<D> items);

  void clear();

  void update(D item, int pos);

  void setItems(List<D> items);

  int size();

  List<D> newCurrentList();
}
