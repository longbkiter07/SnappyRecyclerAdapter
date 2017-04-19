package com.silong.snappyrecycleradapter.adapter;


import com.silong.snappyrecycleradapter.ItemViewHolder;
import com.silong.snappyrecycleradapter.R;
import com.silong.snappyrecycleradapter.model.User;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import me.silong.snappyadapter.RxRecyclerViewCallback;
import me.silong.snappyadapter.RxSortedList;


/**
 * Created by SILONG on 8/28/16.
 */
public class UserRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {

  private final RxSortedList<User> mUserRxSortedList;

  public UserRecyclerViewAdapter(List<User> recyclerList) {
    mUserRxSortedList = new RxSortedList<>(User.class, new RxRecyclerViewCallback<User>(this) {
      @Override
      public boolean areContentsTheSame(User oldData, User newData) {
        return oldData.age == newData.age && oldData.gender == newData.gender;
      }

      @Override
      public boolean areItemsTheSame(User oldData, User newData) {
        return TextUtils.equals(oldData.name, newData.name);
      }

      @Override
      public int compare(User o1, User o2) {
        return o1.name.compareTo(o2.name);
      }
    }, recyclerList);
  }

  public UserRecyclerViewAdapter() {
    mUserRxSortedList = new RxSortedList<>(User.class, new RxRecyclerViewCallback<User>(this) {
      @Override
      public boolean areContentsTheSame(User oldData, User newData) {
        return oldData.age == newData.age && oldData.gender == newData.gender;
      }

      @Override
      public boolean areItemsTheSame(User oldData, User newData) {
        return TextUtils.equals(oldData.name, newData.name);
      }

      @Override
      public int compare(User o1, User o2) {
        return o1.name.compareTo(o2.name);
      }
    });
  }

  public static UserRecyclerViewAdapter newAdapter() {
    return new UserRecyclerViewAdapter();
  }

  public RxSortedList<User> getObservableAdapterManager() {
    return mUserRxSortedList;
  }

  @Override
  public int getItemCount() {
    return mUserRxSortedList.size();
  }

  @Override
  public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
  }

  @Override
  public void onBindViewHolder(ItemViewHolder holder, int position) {
    holder.bind(mUserRxSortedList.getItemAt(position));
  }
}
