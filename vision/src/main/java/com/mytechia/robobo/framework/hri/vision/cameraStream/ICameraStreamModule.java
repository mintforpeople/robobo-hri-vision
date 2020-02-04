package com.mytechia.robobo.framework.hri.vision.cameraStream;

import com.mytechia.robobo.framework.IModule;

public interface ICameraStreamModule extends IModule {
    //void suscribe(ITagListener listener);
    //void unsuscribe(ITagListener listener);
    void useAruco();
    void useAprilTags();
    void pauseDetection();
    void resumeDetection();

    void startServer();
}
