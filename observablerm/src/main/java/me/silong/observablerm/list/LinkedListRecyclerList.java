package me.silong.observablerm.list;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by SILONG on 8/27/16.
 */
class LinkedListRecyclerList<D> implements RecyclerList<D> {

  private LinkedList<D> mItems;

  private ListIterator<D> mListIterator;

  LinkedListRecyclerList() {
    mItems = new LinkedList<>();
    mListIterator = mItems.listIterator();
  }

  LinkedListRecyclerList(List<D> defaultData) {
    mItems = defaultData == null ? new LinkedList<>() : new LinkedList<>(defaultData);
    mListIterator = mItems.listIterator();
  }

  private boolean moveBefore(ListIterator<D> listIterator, int pos) {
    int nextIndex = listIterator.nextIndex();
    if (nextIndex <= pos) {
      while (nextIndex < pos) {
        listIterator.next();
        nextIndex = listIterator.nextIndex();
      }
      return true;
    } else {
      do {
        listIterator.previous();
        nextIndex = listIterator.nextIndex();
      } while (nextIndex > pos);
      return false;
    }
  }

  @Override
  public D getItemAt(int pos) {
    moveBefore(mListIterator, pos);
    return mListIterator.next();
  }

  @Override
  public void add(D item) {
    int currentIndex = mListIterator.nextIndex();
    mItems.add(item);
    mListIterator = mItems.listIterator(currentIndex);
  }

  @Override
  public void add(D item, int pos) {
    int index = mListIterator.nextIndex();
    moveBefore(mListIterator, pos);
    mListIterator.add(item);
    moveBefore(mListIterator, index);
  }

  @Override
  public void remove(int pos) {
    int index = mListIterator.nextIndex();
    if (moveBefore(mListIterator, pos)) {
      mListIterator.next();
    }
    mListIterator.remove();
    moveBefore(mListIterator, index > mItems.size() ? mItems.size() : index);
  }

  @Override
  public int find(D item) {
    return mItems.indexOf(item);
  }

  @Override
  public void addAll(List<D> items, int startPos) {
    int index = mListIterator.nextIndex();
    moveBefore(mListIterator, startPos);
    for (D item : items) {
      mListIterator.add(item);
    }
    moveBefore(mListIterator, index);
  }

  @Override
  public void addAll(List<D> items) {
    mItems.addAll(items);
    mListIterator = mItems.listIterator(mListIterator.nextIndex());
  }

  @Override
  public void clear() {
    mItems.clear();
    mListIterator = mItems.listIterator();
  }

  @Override
  public void update(D item, int pos) {
    int index = mListIterator.nextIndex();
    if (moveBefore(mListIterator, pos)) {
      mListIterator.next();
    }
    mListIterator.set(item);
    moveBefore(mListIterator, index);
  }

  @Override
  public void setItems(List<D> items) {
    int nextPos = mListIterator.nextIndex();
    mItems.clear();
    mItems.addAll(items);
    int newSize = mItems.size();
    int currPos = nextPos > newSize ? newSize : nextPos;
    mListIterator = mItems.listIterator(currPos);
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
