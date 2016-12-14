package com.silong.snappyrecycleradapter;

import com.silong.snappyrecycleradapter.adapter.RegularRecyclerViewAdapter;
import com.silong.snappyrecycleradapter.adapter.UserRecyclerViewAdapter;
import com.silong.snappyrecycleradapter.model.DataFactory;
import com.silong.snappyrecycleradapter.model.User;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;


/**
 * Created by SILONG on 8/28/16.
 */
public class RecyclerViewActivity extends AppCompatActivity {

  private static final String MODE = "mode";

  private static final int MODE_REGULAR = 2;

  private static final int MODE_LINKED_LIST = 0;

  private static final int MODE_ARRAY_LIST = 1;

  private UserRecyclerViewAdapter mUserRecyclerViewAdapter;

  private RegularRecyclerViewAdapter mRegularRecyclerViewAdapter;

  public static Intent newFastLinkedListIntent(Context context) {
    Intent intent = new Intent(context, RecyclerViewActivity.class);
    intent.putExtra(MODE, MODE_LINKED_LIST);
    return intent;
  }

  public static Intent newFastArrayListIntent(Context context) {
    Intent intent = new Intent(context, RecyclerViewActivity.class);
    intent.putExtra(MODE, MODE_ARRAY_LIST);
    return intent;
  }

  public static Intent newRegularIntent(Context context) {
    Intent intent = new Intent(context, RecyclerViewActivity.class);
    intent.putExtra(MODE, MODE_REGULAR);
    return intent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    RecyclerView recyclerView = new RecyclerView(this);
    setContentView(recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    int mode = getIntent().getIntExtra(MODE, MODE_REGULAR);
    DataFactory.fakeUsersToSet(DataFactory.CHUNK).subscribe(users -> {
      switch (mode) {
        case MODE_REGULAR:
          mRegularRecyclerViewAdapter = new RegularRecyclerViewAdapter(users);
          recyclerView.setAdapter(mRegularRecyclerViewAdapter);
          break;
        case MODE_LINKED_LIST:
          mUserRecyclerViewAdapter = UserRecyclerViewAdapter.newLinkedListAdapter();
          mUserRecyclerViewAdapter.getObservableAdapterManager().setItems(users).subscribe();
          recyclerView.setAdapter(mUserRecyclerViewAdapter);
          break;
        case MODE_ARRAY_LIST:
          mUserRecyclerViewAdapter = UserRecyclerViewAdapter.newArrayListAdapter(DataFactory.CHUNK);
          mUserRecyclerViewAdapter.getObservableAdapterManager().setItems(users).subscribe();
          recyclerView.setAdapter(mUserRecyclerViewAdapter);
          break;
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_add_multi:
        if (mRegularRecyclerViewAdapter != null) {
          DataFactory.fakeUsersToAddOrUpdate(mRegularRecyclerViewAdapter.getItemCount(), DataFactory.CHUNK)
              .subscribe(users -> {
                mRegularRecyclerViewAdapter.add(users);
              });
        } else {
          DataFactory.fakeUsersToAddOrUpdate(mUserRecyclerViewAdapter.getItemCount(), DataFactory.CHUNK)
              .flatMap(users -> mUserRecyclerViewAdapter.getObservableAdapterManager().addAll(users))
              .subscribe();
        }
        break;
      case R.id.action_add_multi_at_specific_index:
        if (mRegularRecyclerViewAdapter != null) {
          DataFactory.fakeUsersToAddOrUpdate(mRegularRecyclerViewAdapter.getItemCount(), DataFactory.CHUNK)
              .subscribe(users -> {
                mRegularRecyclerViewAdapter
                    .add(users, mRegularRecyclerViewAdapter.getItemCount() / 2);
              });
        } else {
          DataFactory.fakeUsersToAddOrUpdate(mUserRecyclerViewAdapter.getItemCount(), DataFactory.CHUNK)
              .flatMap(users -> mUserRecyclerViewAdapter.getObservableAdapterManager()
                  .addAll(users, mUserRecyclerViewAdapter.getItemCount() / 2))
              .subscribe();
        }
        break;
      case R.id.action_add_single:
        if (mRegularRecyclerViewAdapter != null) {
          DataFactory.fakeUsersToAddOrUpdate(mRegularRecyclerViewAdapter.getItemCount(), 1)
              .subscribe(users -> {
                mRegularRecyclerViewAdapter.add(users);
              });
        } else {
          DataFactory.fakeUsersToAddOrUpdate(mUserRecyclerViewAdapter.getItemCount(), 1)
              .map(users -> users.get(0))
              .flatMap(user -> mUserRecyclerViewAdapter.getObservableAdapterManager()
                  .add(user))
              .subscribe();
        }
        break;
      case R.id.action_clear:
        if (mRegularRecyclerViewAdapter != null) {
          mRegularRecyclerViewAdapter.clear();
        } else {
          mUserRecyclerViewAdapter.getObservableAdapterManager().clear().subscribe();
        }
        break;
      case R.id.action_remove_one_item:
        if (mRegularRecyclerViewAdapter != null) {
          mRegularRecyclerViewAdapter.remove((int) (Math.random() * mRegularRecyclerViewAdapter.getItemCount() - 1));
        } else {
          mUserRecyclerViewAdapter.getObservableAdapterManager().remove((int) (Math.random() * mUserRecyclerViewAdapter.getItemCount() - 1))
              .subscribe();
        }
        break;
      case R.id.action_set_items:
        if (mRegularRecyclerViewAdapter != null) {
          DataFactory.fakeUsersToSet(DataFactory.CHUNK)
              .subscribe(users -> {
                mRegularRecyclerViewAdapter.setUsers(users);
              });
        } else {
          DataFactory.fakeUsersToSet(DataFactory.CHUNK)
              .flatMap(users -> mUserRecyclerViewAdapter.getObservableAdapterManager().setItems(users))
              .subscribe();
        }
        break;
      case R.id.action_set_one_item:
        if (mRegularRecyclerViewAdapter != null) {
          mRegularRecyclerViewAdapter.setUserAt(new User("custom_name" + Math.random(), 100, User.Gender.male),
              (int) (Math.random() * mRegularRecyclerViewAdapter.getItemCount() - 1));
        } else {
          mUserRecyclerViewAdapter.getObservableAdapterManager().update(
              new User("custom_name" + Math.random(), 100, User.Gender.male),
              (int) (Math.random() * mUserRecyclerViewAdapter.getItemCount() - 1)).subscribe();
        }
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_action, menu);
    return super.onCreateOptionsMenu(menu);
  }

}
