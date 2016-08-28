package com.silong.fastrecyclerviewadapter;

import com.silong.fastrecyclerviewadapter.adapter.FastUserRecyclerViewAdapter;
import com.silong.fastrecyclerviewadapter.adapter.RegularRecyclerViewArapter;
import com.silong.fastrecyclerviewadapter.model.DataFactory;
import com.silong.fastrecyclerviewadapter.model.User;

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

  private FastUserRecyclerViewAdapter mFastUserRecyclerViewAdapter;

  private RegularRecyclerViewArapter mRegularRecyclerViewArapter;

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
    switch (mode) {
      case MODE_REGULAR:
        mRegularRecyclerViewArapter = new RegularRecyclerViewArapter(DataFactory.fakeUsersToSet(DataFactory.CHUNK));
        recyclerView.setAdapter(mRegularRecyclerViewArapter);
        break;
      case MODE_LINKED_LIST:
        mFastUserRecyclerViewAdapter = FastUserRecyclerViewAdapter.newLinkedListAdapter();
        mFastUserRecyclerViewAdapter.setItems(DataFactory.fakeUsersToSet(DataFactory.CHUNK)).subscribe();
        recyclerView.setAdapter(mFastUserRecyclerViewAdapter);
        break;
      case MODE_ARRAY_LIST:
        mFastUserRecyclerViewAdapter = FastUserRecyclerViewAdapter.newArrayListAdapter(DataFactory.CHUNK);
        mFastUserRecyclerViewAdapter.setItems(DataFactory.fakeUsersToSet(DataFactory.CHUNK)).subscribe();
        recyclerView.setAdapter(mFastUserRecyclerViewAdapter);
        break;
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_add_multi:
        if (mRegularRecyclerViewArapter != null) {
          mRegularRecyclerViewArapter
              .add(DataFactory.fakeUsersToAddOrUpdate(mRegularRecyclerViewArapter.getItemCount(), DataFactory.CHUNK));
        } else {
          mFastUserRecyclerViewAdapter
              .addAll(DataFactory.fakeUsersToAddOrUpdate(mFastUserRecyclerViewAdapter.getItemCount(), DataFactory.CHUNK)).subscribe();
        }
        break;
      case R.id.action_add_multi_at_specific_index:
        if (mRegularRecyclerViewArapter != null) {
          mRegularRecyclerViewArapter
              .add(DataFactory.fakeUsersToAddOrUpdate(mRegularRecyclerViewArapter.getItemCount(), DataFactory.CHUNK),
                  mRegularRecyclerViewArapter.getItemCount() / 2);
        } else {
          mFastUserRecyclerViewAdapter
              .addAll(DataFactory.fakeUsersToAddOrUpdate(mFastUserRecyclerViewAdapter.getItemCount(), DataFactory.CHUNK),
                  mFastUserRecyclerViewAdapter.getItemCount() / 2).subscribe();
        }
        break;
      case R.id.action_add_single:
        if (mRegularRecyclerViewArapter != null) {
          mRegularRecyclerViewArapter.add(DataFactory.fakeUsersToAddOrUpdate(mRegularRecyclerViewArapter.getItemCount(), 1));
        } else {
          mFastUserRecyclerViewAdapter.add(DataFactory.fakeUsersToAddOrUpdate(mFastUserRecyclerViewAdapter.getItemCount(), 1).get(0))
              .subscribe();
        }
        break;
      case R.id.action_clear:
        if (mRegularRecyclerViewArapter != null) {
          mRegularRecyclerViewArapter.clear();
        } else {
          mFastUserRecyclerViewAdapter.clear().subscribe();
        }
        break;
      case R.id.action_remove_one_item:
        if (mRegularRecyclerViewArapter != null) {
          mRegularRecyclerViewArapter.remove((int) (Math.random() * mRegularRecyclerViewArapter.getItemCount() - 1));
        } else {
          mFastUserRecyclerViewAdapter.remove((int) (Math.random() * mFastUserRecyclerViewAdapter.getItemCount() - 1)).subscribe();
        }
        break;
      case R.id.action_set_items:
        if (mRegularRecyclerViewArapter != null) {
          mRegularRecyclerViewArapter.setUsers(DataFactory.fakeUsersToSet(DataFactory.CHUNK));
        } else {
          mFastUserRecyclerViewAdapter.setItems(DataFactory.fakeUsersToSet(DataFactory.CHUNK)).subscribe();
        }
        break;
      case R.id.action_set_one_item:
        if (mRegularRecyclerViewArapter != null) {
          mRegularRecyclerViewArapter.setUserAt(new User("custom_name" + Math.random(), 100, User.Gender.male),
              (int) (Math.random() * mRegularRecyclerViewArapter.getItemCount() - 1));
        } else {
          mFastUserRecyclerViewAdapter.update((int) (Math.random() * mFastUserRecyclerViewAdapter.getItemCount() - 1),
              new User("custom_name" + Math.random(), 100, User.Gender.male)).subscribe();
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
