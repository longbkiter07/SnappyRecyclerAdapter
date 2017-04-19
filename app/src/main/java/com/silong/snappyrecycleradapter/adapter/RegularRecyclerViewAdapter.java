package com.silong.snappyrecycleradapter.adapter;

import com.silong.snappyrecycleradapter.ItemViewHolder;
import com.silong.snappyrecycleradapter.R;
import com.silong.snappyrecycleradapter.model.User;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class RegularRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> implements SyncList<User> {

  private List<User> mUsers;

  public RegularRecyclerViewAdapter() {
    mUsers = new ArrayList<>();
  }

  private void sortAndCallDiff(List<User> oldList) {
    Collections.sort(mUsers, new Comparator<User>() {
      @Override
      public int compare(User o1, User o2) {
        return o1.name.compareTo(o2.name);
      }
    });
    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
      @Override
      public int getOldListSize() {
        return mUsers.size();
      }

      @Override
      public int getNewListSize() {
        return oldList.size();
      }

      @Override
      public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        User oldData = mUsers.get(oldItemPosition);
        User newData = oldList.get(newItemPosition);
        return oldData.age == newData.age && oldData.gender == newData.gender && TextUtils.equals(oldData.name, newData.name);
      }

      @Override
      public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        User oldData = mUsers.get(oldItemPosition);
        User newData = oldList.get(newItemPosition);
        return TextUtils.equals(oldData.id, newData.id);
      }
    });
    diffResult.dispatchUpdatesTo(this);
  }

  @Override
  public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
  }

  @Override
  public void onBindViewHolder(ItemViewHolder holder, int position) {
    holder.bind(mUsers.get(position));
  }

  @Override
  public int getItemCount() {
    return mUsers.size();
  }

  public void add(User user) {
    List<User> oldList = new ArrayList<>(mUsers);
    mUsers.add(user);
    sortAndCallDiff(oldList);
  }

  public void add(List<User> users) {
    List<User> oldList = new ArrayList<>(mUsers);
    mUsers.addAll(users);
    sortAndCallDiff(oldList);
  }

  public void remove(User user) {
    List<User> oldList = new ArrayList<>(mUsers);
    mUsers.remove(user);
    sortAndCallDiff(oldList);
  }

  public void remove(int index) {
    List<User> oldList = new ArrayList<>(mUsers);
    mUsers.remove(index);
    sortAndCallDiff(oldList);
  }

  public void clear() {
    int oldSize = mUsers.size();
    mUsers.clear();
    notifyItemRangeRemoved(0, oldSize);
  }

  @Override
  public void set(User user, int pos) {
    List<User> oldList = new ArrayList<>(mUsers);
    mUsers.set(pos, user);
    sortAndCallDiff(oldList);
  }

  @Override
  public void set(List<User> users) {
    List<User> oldList = new ArrayList<>(mUsers);
    mUsers.clear();
    mUsers.addAll(users);
    sortAndCallDiff(oldList);
  }
}