package com.mytechia.robobo.framework.hri.vision.colorDetection;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by luis on 10/8/16.
 */
public interface IColorListener {
//    void onNewColor(int colorrgb, int nearest_color, int x, int y, int height, int width, Bitmap borders);
      void onNewColor(int colorrgb, int nearest_color);

}
