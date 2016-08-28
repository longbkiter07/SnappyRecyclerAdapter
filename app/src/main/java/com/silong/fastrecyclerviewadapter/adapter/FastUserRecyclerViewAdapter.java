package com.silong.fastrecyclerviewadapter.adapter;

import com.silong.fastrecycler.ArrayListRecyclerList;
import com.silong.fastrecycler.DataComparable;
import com.silong.fastrecycler.FastRecyclerViewAdapter;
import com.silong.fastrecycler.LinkedListRecyclerList;
import com.silong.fastrecycler.RecyclerList;
import com.silong.fastrecyclerviewadapter.ItemViewHolder;
import com.silong.fastrecyclerviewadapter.R;
import com.silong.fastrecyclerviewadapter.model.User;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by SILONG on 8/28/16.
 */
public class FastUserRecyclerViewAdapter extends FastRecyclerViewAdapter<User, ItemViewHolder> {

  private FastUserRecyclerViewAdapter(RecyclerList<User> recyclerList) {
    super(recyclerList, new DataComparable<User>() {
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

  public static FastUserRecyclerViewAdapter newLinkedListAdapter() {
    return new FastUserRecyclerViewAdapter(new LinkedListRecyclerList<User>());
  }

  public static FastUserRecyclerViewAdapter newArrayListAdapter(int size) {
    return new FastUserRecyclerViewAdapter(new ArrayListRecyclerList<User>(size));
  }

  @Override
  public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
  }

  @Override
  public void onBindViewHolder(ItemViewHolder holder, int position) {
    holder.bind(getItemAt(position));
  }
}