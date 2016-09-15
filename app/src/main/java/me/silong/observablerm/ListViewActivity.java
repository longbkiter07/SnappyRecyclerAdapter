package me.silong.observablerm;


import me.silong.observablerm.adapter.ListViewAdapter;
import me.silong.observablerm.model.DataFactory;
import me.silong.observablerm.model.User;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

/**
 * Created by SILONG on 8/28/16.
 */
public class ListViewActivity extends AppCompatActivity {

  private ListViewAdapter mListViewAdapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ListView listView = new ListView(this);
    setContentView(listView);
    DataFactory.fakeUsersToSet(DataFactory.CHUNK)
        .subscribe(users -> {
          mListViewAdapter = new ListViewAdapter(users);
          listView.setAdapter(mListViewAdapter);
        });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_add_multi:
        DataFactory.fakeUsersToAddOrUpdate(mListViewAdapter.getCount(), DataFactory.CHUNK)
            .subscribe(users -> {
              mListViewAdapter.add(users);
            });
        break;
      case R.id.action_add_multi_at_specific_index:
        DataFactory.fakeUsersToAddOrUpdate(mListViewAdapter.getCount(), DataFactory.CHUNK)
            .subscribe(users -> {
              mListViewAdapter
                  .add(users, mListViewAdapter.getCount() / 2);
            });
        break;
      case R.id.action_add_single:
        DataFactory.fakeUsersToAddOrUpdate(mListViewAdapter.getCount(), 1)
            .subscribe(users -> {
              mListViewAdapter.add(users);
            });
        break;
      case R.id.action_clear:
        mListViewAdapter.clear();
        break;
      case R.id.action_remove_one_item:
        mListViewAdapter.remove((int) (Math.random() * mListViewAdapter.getCount() - 1));
        break;
      case R.id.action_set_items:
        DataFactory.fakeUsersToSet(DataFactory.CHUNK)
            .subscribe(users -> {
              mListViewAdapter.setUsers(users);
            });
        break;
      case R.id.action_set_one_item:
        mListViewAdapter.setUserAt(new User("custom_name" + Math.random(), 100, User.Gender.male),
            (int) (Math.random() * mListViewAdapter.getCount() - 1));
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
