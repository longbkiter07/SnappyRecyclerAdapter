package me.silong.observablerm.list;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by SILONG on 8/27/16.
 */
public class RecyclerLinkedList<D> extends LinkedList<D> {

  private ListIterator<D> mListIterator;

  public RecyclerLinkedList() {
    mListIterator = listIterator();
  }

  public RecyclerLinkedList(List<D> defaultData) {
    super(defaultData);
    mListIterator = listIterator();
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
  public D get(int index) {
    try {
      int nextIndex = mListIterator.nextIndex();
      try {
        moveBefore(mListIterator, index);
      } catch (Exception e) {
        mListIterator = listIterator(nextIndex);
        moveBefore(mListIterator, index);
      }
      return mListIterator.next();
    } catch (Exception e) {
      return super.get(index);
    }
  }

}
