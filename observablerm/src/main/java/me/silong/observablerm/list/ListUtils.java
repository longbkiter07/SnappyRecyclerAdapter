package me.silong.observablerm.list;

import java.util.List;

import me.silong.observablerm.ObservableAdapterManager;

/**
 * Created by SILONG on 9/15/16.
 */
public class ListUtils {

  private ListUtils() {

  }

  public static <D> RecyclerList<D> createList(List<D> defaultData, ObservableAdapterManager.ListType listType) {
    switch (listType) {
      case array_list:
        return new ArrayListRecyclerList<>(defaultData);
      case linked_list:
        return new LinkedListRecyclerList<>(defaultData);
      default:
        return null;
    }
  }

  public static <D> RecyclerList<D> createLinkedList() {
    return new LinkedListRecyclerList<>();
  }

  public static <D> RecyclerList<D> createArrayList() {
    return new ArrayListRecyclerList<>();
  }

  public static <D> RecyclerList<D> createArrayList(int defaultSize) {
    return new ArrayListRecyclerList<>(defaultSize);
  }

}
