# SnappyRecyclerAdapter

This library helps you to manage `RecyclerViewAdapter` easier. 

# Features

* Calling `notifyDataSetChanges`, `notifyItemInserted`, `notifyItemChanged`... and apply [DiffUtil] (https://developer.android.com/reference/android/support/v7/util/DiffUtil.html) automatically so you don't care about them anymore.
* Calling `DiffUtil` in background thread so that the ui does not lag.
* Queueing events to keep data is consistent.
* Observable method so that we can catch the callback to do next action.
* Supporting LinkedList, ArrayList for reasonable usage.

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

Code example: read [here] (https://github.com/longbkiter07/SnappyRecyclerAdapter/blob/master/app/src/main/java/com/silong/snappyrecycleradapter/adapter/UserRecyclerViewAdapter.java)

## Create your RecyclerList:

```
List<T> recyclerList = new ArrayList<>();
```
Or
```
List<T> recyclerList = new RecyclerLinkedList<>();
```
`RecyclerLinkedList` is a list that helps you to improve performance for `get(int index)` method.
## Create your RecyclerViewAdapter:

```
public class MyAdapter<VH> extends RecyclerViewAdapter<VH> {
    private final ObservableAdapterManager<User> mObservableAdapterManager;
    public MyRecyclerViewAdapter(List<User> items) {
      mObservableAdapterManager = new ObservableAdapterManager<User>(this, items, null);
    }
    @Override
    public int getItemCount() {
      return mObservableAdapterManager.getItemCount();
    }
    ...
}
```

If you want to use `DiffUtil`, you would implement `DataComparable` to define differences between 2 items. Otherwise, you can pass `null` value.

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
