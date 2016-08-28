package com.silong.fastrecycler;

import com.silong.fastrecycler.logs.Ln;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by SILONG on 8/27/16.
 */
public class LinkedListRecyclerList<D> implements RecyclerList<D> {

  private List<D> mItems;

  private ListIterator<D> mGettingIterator, mTopIterator, mBottomIterator;

  public LinkedListRecyclerList() {
    mItems = new LinkedList<>();
    mGettingIterator = mItems.listIterator();
    mTopIterator = mItems.listIterator();
    mBottomIterator = mItems.listIterator();
  }

  private boolean moveBefore(ListIterator<D> listIterator, int pos) {
    int nextIndex = listIterator.nextIndex();
    Ln.d("moving:" + nextIndex + " to pos:" + pos + ", size:" + mItems.size());
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
    Ln.d("getItemAt:" + pos);
    moveBefore(mGettingIterator, pos);
    return mGettingIterator.next();
  }

  @Override
  public void add(D item) {
    Ln.d("add:" + item);
    add(item, mItems.size());
  }

  @Override
  public void add(D item, int pos) {
    Ln.d("add:" + item + ", pos:" + pos);
    ListIterator<D> selectingLisIterator = selectIterator(pos);
    moveBefore(selectingLisIterator, pos);
    selectingLisIterator.add(item);
    restoreIterator();
  }

  private void restoreIterator() {
    int size = mItems.size();
    int currentIndex = mGettingIterator.nextIndex();
    if (currentIndex < 0) {
      currentIndex = 0;
    } else if (currentIndex > size) {
      currentIndex = size;
    }
    mTopIterator = mItems.listIterator(0);
    mBottomIterator = mItems.listIterator(size);
    mGettingIterator = mItems.listIterator(currentIndex);
  }


  private ListIterator<D> selectIterator(int pos) {
    return pos < mItems.size() / 2 ? mTopIterator : mBottomIterator;
  }

  @Override
  public void remove(int pos) {
    Ln.d("remove:" + pos);
    ListIterator<D> selectingLisIterator = selectIterator(pos);
    if (moveBefore(selectingLisIterator, pos)) {
      selectingLisIterator.next();
    }
    selectingLisIterator.remove();
    restoreIterator();
  }

  @Override
  public int find(D item) {
    return mItems.indexOf(item);
  }

  @Override
  public void addAll(List<D> items, int startPos) {
    Ln.d("add all:" + items.size() + ",pos:" + startPos);
    ListIterator<D> selectingIterator = selectIterator(startPos);
    moveBefore(selectingIterator, startPos);
    for (D item : items) {
      selectingIterator.add(item);
    }
    restoreIterator();
  }

  @Override
  public void addAll(List<D> items) {
    Ln.d("add all:" + items.size());
    addAll(items, mItems.size());
  }

  @Override
  public void clear() {
    Ln.d("clear");
    mItems.clear();
    mGettingIterator = mItems.listIterator();
    mTopIterator = mItems.listIterator(0);
    mBottomIterator = mItems.listIterator(0);
  }

  @Override
  public void update(D item, int pos) {
    Ln.d("update:" + item + ", pos:" + pos);
    ListIterator<D> selectingIterator = selectIterator(pos);
    if (moveBefore(selectingIterator, pos)) {
      selectingIterator.next();
    }
    selectingIterator.set(item);
    restoreIterator();
  }

  @Override
  public void setItems(List<D> items) {
    Ln.d("set items:" + items.size());
    int nextPos = mGettingIterator.nextIndex();
    mItems.clear();
    mItems.addAll(items);
    int newSize = mItems.size();
    int currPos = nextPos > newSize ? newSize : nextPos;
    mGettingIterator = mItems.listIterator(currPos);
    mTopIterator = mItems.listIterator();
    mBottomIterator = mItems.listIterator(mItems.size());
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
