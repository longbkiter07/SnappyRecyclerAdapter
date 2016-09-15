package me.silong.observablerm;

import me.silong.observablerm.model.User;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by SILONG on 8/28/16.
 */
public class ItemViewHolder extends RecyclerView.ViewHolder {

  private TextView vName, vAge, vGender;

  public ItemViewHolder(View itemView) {
    super(itemView);
    vName = (TextView) itemView.findViewById(R.id.item_name);
    vAge = (TextView) itemView.findViewById(R.id.item_age);
    vGender = (TextView) itemView.findViewById(R.id.item_gender);
  }

  public void bind(User user) {
    vName.setText(user.name);
    vAge.setText(String.format(Locale.US, "%d years old", user.age));
    vGender.setText(user.gender.name());
  }
}
