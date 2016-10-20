package com.mytechia.robobo.framework.hri.vision.colorDetection;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.HashSet;

/**
 * Created by luis on 9/8/16.
 */
public abstract class AColorDetectionModule implements IColorDetectionModule {
    private HashSet<IColorListener> listeners = new HashSet<IColorListener>();

    @Override
    public void suscribe(IColorListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(IColorListener listener) {
        listeners.remove(listener);
    }

//    protected void notifyColor(int colorrgb, int nearest_color, int x, int y, int height, int width, Bitmap borders){
//        for (IColorListener listener:listeners){
//            listener.onNewColor(colorrgb,nearest_color,x,y,height,width,borders);
//
//        }
//    }

    protected void notifyColor(int colorrgb, int nearest_color){
        for (IColorListener listener:listeners){
            listener.onNewColor(colorrgb,nearest_color);

        }
    }

}
