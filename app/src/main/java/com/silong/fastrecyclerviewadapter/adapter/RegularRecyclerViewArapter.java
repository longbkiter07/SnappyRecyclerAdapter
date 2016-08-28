package com.silong.fastrecyclerviewadapter.adapter;

import com.silong.fastrecyclerviewadapter.ItemViewHolder;
import com.silong.fastrecyclerviewadapter.R;
import com.silong.fastrecyclerviewadapter.model.User;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

public class RegularRecyclerViewArapter extends RecyclerView.Adapter<ItemViewHolder> {

  private final List<User> mUsers;

  public RegularRecyclerViewArapter(List<User> users) {
    mUsers = users;
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

  public void add(User user, int position) {
    mUsers.add(position, user);
    notifyItemInserted(position);
  }

  public void add(List<User> users, int position) {
    mUsers.addAll(position, users);
    notifyItemRangeInserted(position, users.size());
  }

  public void add(List<User> users) {
    mUsers.addAll(users);
    notifyItemRangeInserted(mUsers.size() - users.size(), users.size());
  }

  public void add(User user) {
    mUsers.add(user);
    notifyDataSetChanged();
  }

  public void setUserAt(User user, int pos) {
    mUsers.set(pos, user);
    notifyItemChanged(pos);
  }

  public void remove(User user) {
    int index = mUsers.indexOf(user);
    mUsers.remove(index);
    notifyItemRemoved(index);
  }

  public void remove(int index) {
    mUsers.remove(index);
    notifyItemRemoved(index);
  }

  public void setUsers(List<User> users) {
    mUsers.clear();
    mUsers.addAll(users);
    notifyDataSetChanged();
  }

  public void clear() {
    mUsers.clear();
    notifyDataSetChanged();
  }
}