package com.silong.snappyrecycleradapter;

import com.codemonkeylabs.fpslibrary.FrameDataCallback;
import com.codemonkeylabs.fpslibrary.TinyDancer;

import android.app.Application;
import android.util.Log;

/**
 * Created by SILONG on 8/29/16.
 */
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    //    TinyDancer.create()
    //        .show(this);

    //alternatively
    //    TinyDancer.create()
    //        .redFlagPercentage(.1f) // set red indicator for 10%....different from default
    //        .startingXPosition(200)
    //        .startingYPosition(600)
    //        .show(this);

    //you can add a callback to get frame times and the calculated
    //number of dropped frames within that window
    TinyDancer.create()
        .addFrameDataCallback(new FrameDataCallback() {
          @Override
          public void doFrame(long previousFrameNS, long currentFrameNS, int droppedFrames) {
            //collect your stats here
            long renderTime = (currentFrameNS - previousFrameNS) / 1000000;
            if (renderTime > 35) {
              Log.d("App", "rendered frame:" + renderTime);
            }
          }
        })
        .show(this);
  }

}
