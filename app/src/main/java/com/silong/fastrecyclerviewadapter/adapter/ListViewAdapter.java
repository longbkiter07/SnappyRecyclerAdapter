package com.silong.fastrecyclerviewadapter.adapter;

import com.silong.fastrecyclerviewadapter.ItemViewHolder;
import com.silong.fastrecyclerviewadapter.R;
import com.silong.fastrecyclerviewadapter.model.User;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class ListViewAdapter extends BaseAdapter {

  private final List<User> mUsers;

  public ListViewAdapter(List<User> users) {
    mUsers = users;
  }

  @Override
  public int getCount() {
    return mUsers.size();
  }

  @Override
  public User getItem(int i) {
    return mUsers.get(i);
  }

  @Override
  public long getItemId(int i) {
    return mUsers.get(i).hashCode();
  }


  public void add(User user, int position) {
    mUsers.add(position, user);
    notifyDataSetChanged();
  }

  public void add(List<User> users, int position) {
    mUsers.addAll(position, users);
    notifyDataSetChanged();
  }

  public void add(List<User> users) {
    mUsers.addAll(users);
    notifyDataSetChanged();
  }

  public void add(User user) {
    mUsers.add(user);
    notifyDataSetChanged();
  }

  public void setUserAt(User user, int pos) {
    mUsers.set(pos, user);
    notifyDataSetChanged();
  }

  public void remove(User user) {
    mUsers.remove(user);
    notifyDataSetChanged();
  }

  public void remove(int index) {
    mUsers.remove(index);
    notifyDataSetChanged();
  }

  public void setUsers(List<User> users) {
    mUsers.clear();
    mUsers.addAll(users);
    notifyDataSetChanged();
  }

  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {
    ItemViewHolder itemViewHolder;
    if (view == null) {
      view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
      itemViewHolder = new ItemViewHolder(view);
      view.setTag(itemViewHolder);
    } else {
      itemViewHolder = (ItemViewHolder) view.getTag();
    }
    itemViewHolder.bind(getItem(i));
    return view;
  }

  public void clear() {
    mUsers.clear();
    notifyDataSetInvalidated();
  }
}