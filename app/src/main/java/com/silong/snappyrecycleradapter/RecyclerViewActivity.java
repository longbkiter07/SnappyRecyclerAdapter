package com.silong.snappyrecycleradapter;

import com.silong.snappyrecycleradapter.adapter.RegularRecyclerViewAdapter;
import com.silong.snappyrecycleradapter.adapter.RxUserRecyclerViewAdapter;
import com.silong.snappyrecycleradapter.adapter.SortedListRecyclerViewAdapter;
import com.silong.snappyrecycleradapter.adapter.SyncList;
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

  private static final int MODE_SORTED_LIST = 1;

  private static final int MODE_RXSORTED_LIST = 0;

  private RxUserRecyclerViewAdapter mRxUserRecyclerViewAdapter;

  private SyncList<User> mSyncAdapter;

  public static Intent newRxSortedList(Context context, String name) {
    Intent intent = new Intent(context, RecyclerViewActivity.class);
    intent.putExtra(MODE, MODE_RXSORTED_LIST);
    intent.putExtra("name", name);
    return intent;
  }

  public static Intent newSortedIntent(Context context, String name) {
    Intent intent = new Intent(context, RecyclerViewActivity.class);
    intent.putExtra(MODE, MODE_SORTED_LIST);
    intent.putExtra("name", name);
    return intent;
  }

  public static Intent newRegularIntent(Context context, String name) {
    Intent intent = new Intent(context, RecyclerViewActivity.class);
    intent.putExtra(MODE, MODE_REGULAR);
    intent.putExtra("name", name);
    return intent;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle(getIntent().getStringExtra("name"));
    RecyclerView recyclerView = new RecyclerView(this);
    setContentView(recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    int mode = getIntent().getIntExtra(MODE, MODE_REGULAR);
    DataFactory.fakeUsersToSet(DataFactory.CHUNK).subscribe(users -> {
      switch (mode) {
        case MODE_REGULAR:
          RegularRecyclerViewAdapter regularRecyclerViewAdapter = new RegularRecyclerViewAdapter();
          mSyncAdapter = regularRecyclerViewAdapter;
          mSyncAdapter.set(users);
          recyclerView.setAdapter(regularRecyclerViewAdapter);
          break;
        case MODE_SORTED_LIST:
          SortedListRecyclerViewAdapter sortedListRecyclerViewAdapter = new SortedListRecyclerViewAdapter();
          mSyncAdapter = sortedListRecyclerViewAdapter;
          mSyncAdapter.set(users);
          recyclerView.setAdapter(sortedListRecyclerViewAdapter);
          break;
        case MODE_RXSORTED_LIST:
          mRxUserRecyclerViewAdapter = RxUserRecyclerViewAdapter.newAdapter();
          mRxUserRecyclerViewAdapter.getObservableAdapterManager().set(users).subscribe();
          recyclerView.setAdapter(mRxUserRecyclerViewAdapter);
          break;
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_add_multi:
        if (mSyncAdapter != null) {
          DataFactory.fakeUsersToAddOrUpdate(mSyncAdapter.getItemCount(), DataFactory.CHUNK)
              .subscribe(users -> {
                mSyncAdapter.add(users);
              });
        } else {
          DataFactory.fakeUsersToAddOrUpdate(mRxUserRecyclerViewAdapter.getItemCount(), DataFactory.CHUNK)
              .flatMap(users -> mRxUserRecyclerViewAdapter.getObservableAdapterManager().addAll(users))
              .subscribe();
        }
        break;
      case R.id.action_add_multi_at_specific_index:
        if (mSyncAdapter != null) {
          DataFactory.fakeUsersToAddOrUpdate(mSyncAdapter.getItemCount(), DataFactory.CHUNK)
              .subscribe(users -> {
                mSyncAdapter
                    .add(users);
              });
        } else {
          DataFactory.fakeUsersToAddOrUpdate(mRxUserRecyclerViewAdapter.getItemCount(), DataFactory.CHUNK)
              .flatMap(users -> mRxUserRecyclerViewAdapter.getObservableAdapterManager()
                  .addAll(users))
              .subscribe();
        }
        break;
      case R.id.action_add_single:
        if (mSyncAdapter != null) {
          DataFactory.fakeUsersToAddOrUpdate(mSyncAdapter.getItemCount(), 1)
              .subscribe(users -> {
                mSyncAdapter.add(users.get(0));
              });
        } else {
          DataFactory.fakeUsersToAddOrUpdate(mRxUserRecyclerViewAdapter.getItemCount(), 1)
              .map(users -> users.get(0))
              .flatMap(user -> mRxUserRecyclerViewAdapter.getObservableAdapterManager()
                  .add(user))
              .subscribe();
        }
        break;
      case R.id.action_clear:
        if (mSyncAdapter != null) {
          mSyncAdapter.clear();
        } else {
          mRxUserRecyclerViewAdapter.getObservableAdapterManager().clear().subscribe();
        }
        break;
      case R.id.action_remove_one_item:
        if (mSyncAdapter != null) {
          mSyncAdapter.remove((int) (Math.random() * mSyncAdapter.getItemCount() - 1));
        } else {
          mRxUserRecyclerViewAdapter.getObservableAdapterManager()
              .removeAt((int) (Math.random() * mRxUserRecyclerViewAdapter.getItemCount() - 1))
              .subscribe();
        }
        break;
      case R.id.action_set_items:
        if (mSyncAdapter != null) {
          DataFactory.fakeUsersToSet(DataFactory.CHUNK)
              .subscribe(users -> {
                mSyncAdapter.set(users);
              });
        } else {
          DataFactory.fakeUsersToSet(DataFactory.CHUNK)
              .flatMap(users -> mRxUserRecyclerViewAdapter.getObservableAdapterManager().set(users))
              .subscribe();
        }
        break;
      case R.id.action_set_one_item:
        int i = (int) (Math.random() * 100);
        if (mSyncAdapter != null) {
          mSyncAdapter.set(new User("User_" + i, "custom_name " + Math.random(), 100, User.Gender.male),
              (int) (Math.random() * mSyncAdapter.getItemCount() - 1));
        } else {
          mRxUserRecyclerViewAdapter.getObservableAdapterManager().updateItemAt(
              (int) (Math.random() * mRxUserRecyclerViewAdapter.getItemCount() - 1),
              new User("User_" + i, "custom_name" + Math.random(), 100, User.Gender.male)).subscribe();
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
