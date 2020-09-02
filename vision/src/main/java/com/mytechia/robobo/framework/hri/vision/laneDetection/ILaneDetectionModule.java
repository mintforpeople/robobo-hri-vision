package com.mytechia.robobo.framework.hri.vision.laneDetection;

import com.mytechia.robobo.framework.IModule;

public interface ILaneDetectionModule extends IModule {
    void suscribe(ILaneDetectionListener listener);
    void unsuscribe(ILaneDetectionListener listener);
    void pauseDetection();
    void resumeDetection();
}
