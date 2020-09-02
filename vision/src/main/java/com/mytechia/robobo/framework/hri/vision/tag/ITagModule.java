package com.mytechia.robobo.framework.hri.vision.tag;

import com.mytechia.robobo.framework.IModule;

public interface ITagModule extends IModule {
    void suscribe(ITagListener listener);
    void unsuscribe(ITagListener listener);
    void useAruco();
    void useAprilTags();
    void pauseDetection();
    void resumeDetection();
}
