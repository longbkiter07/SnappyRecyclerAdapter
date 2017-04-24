# SnappyRecyclerAdapter

This library helps you to manage `RecyclerViewAdapter` easier. This library is very useful for rendering a sorted data in real time (For example: chat application)

# Features

* Calling `notifyDataSetChanges`, `notifyItemInserted`, `notifyItemChanged`... automatically
* Applying both `SortedList` and `DiffUtil`
* Calling `DiffUtil` in background thread so that the ui does not lag
* Queueing events to keep data is consistent.
* Observable method so that we can catch the callback to do next action.

# Usage

This library requires RxJava / RxAndroid and AppCompat version 24.2.+(or newer) library to use.

```
compile 'io.reactivex:rxandroid:{lastest_version}'
compile 'io.reactivex:rxjava:{lastest_version}'
compile ('me.silong:observablerm:{latest_version}'){
    transitive = true
}
compile 'com.android.support:appcompat-v7:24.+'
compile 'com.android.support:recyclerview-v7:24.+'

```

Code example: please read sample app

## Create your RecyclerViewAdapter:

```
public class MyAdapter<VH> extends RecyclerViewAdapter<VH> {
    private final RxSortedList<T> mRxSortedList;
    public MyRecyclerViewAdapter() {
      mRxSortedList = new RxSortedList<>(T.class, new RxRecyclerViewCallback<T>(this) {
            @Override
            public boolean areContentsTheSame(T oldData, T newData) {
              return //return whether content of oldData and newData are the same
            }
      
            @Override
            public boolean areItemsTheSame(T oldData, T newData) {
              return //return whether oldData and newData are the same (checking object id is recommended)
            }
      
            @Override
            public int compare(T o1, T o2) {
              return //sort order. 
            }
          });
    }
    @Override
    public int getItemCount() {
      return mRxSortedList.size();
    }
    ...
}
```

## Add item:

```
mObservableAdapterManager.add(item).subscribe();
```

## Add item with callback:

```
mObservableAdapterManager.add(item).subscribe(o -> { ... }, throwable -> { ... }); 
```

The library also supports `remove`, `set`, `clear` method.

# Issue

Welcome everyone to discuss about this library [here] (https://github.com/longbkiter07/SnappyRecyclerAdapter/issues).

# Thanks

Thanks to [Henry](https://github.com/henrytao-me/) for deployment script.

# License
```
Copyright(c) 2016 "Si Long <long.bkiter07@gmail.com>"

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
