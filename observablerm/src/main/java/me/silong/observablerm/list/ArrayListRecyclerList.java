package me.silong.observablerm.list;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SILONG on 8/27/16.
 */
public class ArrayListRecyclerList<D> implements RecyclerList<D> {

  private final ArrayList<D> mItems;

  public ArrayListRecyclerList() {
    mItems = new ArrayList<>();
  }

  public ArrayListRecyclerList(int startSize) {
    mItems = new ArrayList<>(startSize);
  }

  @Override
  public D getItemAt(int pos) {
    return mItems.get(pos);
  }

  @Override
  public void add(D item) {
    mItems.add(item);
  }

  @Override
  public void add(D item, int pos) {
    mItems.add(pos, item);
  }

  @Override
  public void remove(int pos) {
    mItems.remove(pos);
  }

  @Override
  public int find(D item) {
    return mItems.indexOf(item);
  }

  @Override
  public void addAll(List<D> items, int startPos) {
    mItems.addAll(startPos, items);
  }

  @Override
  public void addAll(List<D> items) {
    mItems.addAll(items);
  }

  @Override
  public void update(D item, int pos) {
    mItems.set(pos, item);
  }

  @Override
  public void clear() {
    mItems.clear();
  }

  @Override
  public void setItems(List<D> items) {
    mItems.clear();
    mItems.addAll(items);
  }

  @Override
  public int size() {
    return mItems.size();
  }

  @Override
  public List<D> newCurrentList() {
    return new ArrayList<>(mItems);
  }
}
