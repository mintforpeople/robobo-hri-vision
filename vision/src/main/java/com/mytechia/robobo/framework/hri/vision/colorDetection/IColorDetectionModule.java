package com.mytechia.robobo.framework.hri.vision.colorDetection;

import android.graphics.Bitmap;

import com.mytechia.robobo.framework.IModule;

/**
 * Created by luis on 9/8/16.
 */
public interface IColorDetectionModule extends IModule {

    void suscribe(IColorListener listener);
    void unsuscribe(IColorListener listener);
    void startDetection();
    void pauseDetection();
}
