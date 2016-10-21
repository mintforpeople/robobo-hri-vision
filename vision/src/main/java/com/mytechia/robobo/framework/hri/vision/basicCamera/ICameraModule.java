package com.mytechia.robobo.framework.hri.vision.basicCamera;

import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import com.mytechia.robobo.framework.IModule;

import org.opencv.android.CameraBridgeViewBase;

/**
 * Created by luis on 19/7/16.
 */
public interface ICameraModule extends IModule {

    public void suscribe(ICameraListener listener);

    public void unsuscribe(ICameraListener listener);

    public void signalInit();

    public void passSurfaceView(SurfaceView view);

    public void passOCVthings(CameraBridgeViewBase bridgebase);

    public void changeCamera();
}
