package com.mytechia.robobo.framework.hri.vision.objectRecognition;

import com.mytechia.robobo.framework.IModule;

public interface IObjectRecognitionModule extends IModule {

    void suscribe(IObjectRecognizerListener listener);
    void unsuscribe(IObjectRecognizerListener listener);
    void setConfidence(Float confLevel);
    void setMaxDetections(Integer maxDetectionNumber);
    void pauseDetection();
    void resumeDetection();

}
