package com.mytechia.robobo.framework.hri.vision.faceDetection;

import com.mytechia.robobo.framework.IModule;

/**
 * Created by luis on 24/7/16.
 */
public interface IFaceDetectionModule extends IModule{
    public void suscribe(IFaceListener listener);
    public void unsuscribe(IFaceListener listener);

}
