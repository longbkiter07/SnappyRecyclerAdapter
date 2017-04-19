package com.silong.snappyrecycleradapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by SILONG on 8/28/16.
 */
public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ListView listView = (ListView) findViewById(android.R.id.list);
    listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
        new String[]{
            "RxSortedList",
            "SortedList",
            "RegularList"
        }));
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent;
        switch (i) {
          default:
          case 0:
            intent = RecyclerViewActivity.newRxSortedList(MainActivity.this, listView.getItemAtPosition(i).toString());
            break;
          case 1:
            intent = RecyclerViewActivity.newSortedIntent(MainActivity.this, listView.getItemAtPosition(i).toString());
            break;
          case 2:
            intent = RecyclerViewActivity.newRegularIntent(MainActivity.this, listView.getItemAtPosition(i).toString());
            break;
        }
        startActivity(intent);
      }
    });
  }
}
