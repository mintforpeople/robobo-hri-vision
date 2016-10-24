package com.mytechia.robobo.framework.hri.vision.faceRecognition;

import com.mytechia.robobo.framework.IModule;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;

/**
 * Created by luis on 24/10/16.
 */

public interface IFaceRecognitionModule extends IModule {
    public void train(Frame frame, int label);
    public void identify(Frame frame);
}
