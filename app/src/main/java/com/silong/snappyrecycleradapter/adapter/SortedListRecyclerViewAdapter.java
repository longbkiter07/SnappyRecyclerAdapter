package com.silong.snappyrecycleradapter.adapter;

import com.silong.snappyrecycleradapter.ItemViewHolder;
import com.silong.snappyrecycleradapter.R;
import com.silong.snappyrecycleradapter.model.User;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;


public class SortedListRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> implements SyncList<User> {

  private SortedList<User> mSortedList;

  public SortedListRecyclerViewAdapter() {
    mSortedList = new SortedList<>(User.class, new SortedListAdapterCallback<User>(this) {

      @Override
      public boolean areContentsTheSame(User oldData, User newData) {
        return oldData.age == newData.age && oldData.gender == newData.gender && TextUtils.equals(oldData.name, newData.name);
      }

      @Override
      public boolean areItemsTheSame(User oldData, User newData) {
        return TextUtils.equals(oldData.id, newData.id);
      }

      @Override
      public int compare(User o1, User o2) {
        return o1.name.compareTo(o2.name);
      }
    });
  }

  @Override
  public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
  }

  @Override
  public void onBindViewHolder(ItemViewHolder holder, int position) {
    holder.bind(mSortedList.get(position));
  }

  @Override
  public int getItemCount() {
    return mSortedList.size();
  }

  public void add(User user) {
    long time = System.nanoTime();
    mSortedList.add(user);
  }

  public void add(List<User> users) {
    mSortedList.addAll(users);
  }

  public void remove(User user) {
    mSortedList.remove(user);
  }

  public void remove(int index) {
    mSortedList.removeItemAt(index);
  }

  public void clear() {
    mSortedList.clear();
  }

  @Override
  public void set(User user, int pos) {
    mSortedList.updateItemAt(pos, user);
  }

  @Override
  public void set(List<User> users) {
    mSortedList.clear();
    mSortedList.addAll(users);
  }
}