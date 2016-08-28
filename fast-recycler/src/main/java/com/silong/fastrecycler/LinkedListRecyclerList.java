package com.silong.fastrecycler;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by SILONG on 8/27/16.
 */
public class LinkedListRecyclerList<D> implements RecyclerList<D> {

  private static final String TAG = LinkedListRecyclerList.class.getSimpleName();

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
    Log.d(TAG, "moving:" + nextIndex + " to pos:" + pos + ", size:" + mItems.size());
    if (nextIndex <= pos) {
      while (nextIndex < pos){
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
    moveBefore(mGettingIterator, pos);
    return mGettingIterator.next();
  }

  @Override
  public void add(D item) {
    add(item, mItems.size());
  }

  @Override
  public void add(D item, int pos) {
    ListIterator<D> selectingLisIterator = selectIterator(pos);
    moveBefore(selectingLisIterator, pos);
    selectingLisIterator.add(item);
    restoreIterator(selectingLisIterator);
  }

  private void restoreIterator(ListIterator<D> listIterator) {
    if (listIterator == mTopIterator) {
      mTopIterator = mItems.listIterator();
    } else if (listIterator == mBottomIterator) {
      mBottomIterator = mItems.listIterator(mItems.size());
    }
  }


  private ListIterator<D> selectIterator(int pos) {
    return pos < mItems.size() / 2 ? mTopIterator : mBottomIterator;
  }

  @Override
  public void remove(int pos) {
    ListIterator<D> selectingLisIterator = selectIterator(pos);
    if (moveBefore(selectingLisIterator, pos)) {
      selectingLisIterator.next();
    }
    selectingLisIterator.remove();
    restoreIterator(selectingLisIterator);
  }

  @Override
  public void remove(D item) {
    int nextPos = mGettingIterator.nextIndex();
    mItems.remove(item);
    int newSize = mItems.size();
    int currPos = nextPos > newSize ? newSize : nextPos;
    mGettingIterator = mItems.listIterator(currPos);
    mTopIterator = mItems.listIterator();
    mBottomIterator = mItems.listIterator(mItems.size());
  }

  @Override
  public void addAll(List<D> items, int startPos) {
    ListIterator<D> selectingIterator = selectIterator(startPos);
    moveBefore(selectingIterator, startPos);
    for (D item : items) {
      selectingIterator.add(item);
    }
    restoreIterator(selectingIterator);
  }

  @Override
  public void addAll(List<D> items) {
    addAll(items, mItems.size());
  }

  @Override
  public void clear() {
    mItems.clear();
    mGettingIterator = mItems.listIterator();
  }

  @Override
  public void update(D item, int pos) {
    ListIterator<D> selectingIterator = selectIterator(pos);
    if (moveBefore(selectingIterator, pos)) {
      selectingIterator.next();
    }
    selectingIterator.set(item);
    restoreIterator(selectingIterator);
  }

  @Override
  public void setItems(List<D> items) {
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
