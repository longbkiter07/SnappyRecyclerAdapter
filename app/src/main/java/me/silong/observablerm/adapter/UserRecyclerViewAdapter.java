package me.silong.observablerm.adapter;

import me.silong.observablerm.DataComparable;
import me.silong.observablerm.ObservableAdapterManager;
import me.silong.observablerm.list.ArrayListRecyclerList;
import me.silong.observablerm.list.LinkedListRecyclerList;
import me.silong.observablerm.list.RecyclerList;
import me.silong.observablerm.ItemViewHolder;
import me.silong.observablerm.R;
import me.silong.observablerm.model.User;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by SILONG on 8/28/16.
 */
public class UserRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {

  private final ObservableAdapterManager<User> mObservableAdapterManager;

  public UserRecyclerViewAdapter(RecyclerList<User> recyclerList) {
    mObservableAdapterManager = new ObservableAdapterManager<User>(this, recyclerList,
        new DataComparable<User>() {
          @Override
          public boolean areContentsTheSame(User oldData, User newData) {
            return oldData.age == newData.age && oldData.gender == newData.gender;
          }

          @Override
          public boolean areItemsTheSame(User oldData, User newData) {
            return TextUtils.equals(oldData.name, newData.name);
          }
        });
  }

  public static UserRecyclerViewAdapter newLinkedListAdapter() {
    return new UserRecyclerViewAdapter(new LinkedListRecyclerList<User>());
  }

  public static UserRecyclerViewAdapter newArrayListAdapter(int size) {
    return new UserRecyclerViewAdapter(new ArrayListRecyclerList<User>(size));
  }

  public ObservableAdapterManager<User> getObservableAdapterManager() {
    return mObservableAdapterManager;
  }

  @Override
  public int getItemCount() {
    return mObservableAdapterManager.getItemCount();
  }

  @Override
  public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
  }

  @Override
  public void onBindViewHolder(ItemViewHolder holder, int position) {
    holder.bind(mObservableAdapterManager.getItemAt(position));
  }
}
