package com.mytechia.robobo.framework.hri.vision.lineDetection;

import com.mytechia.robobo.framework.IModule;

import org.opencv.core.Mat;
import org.opencv.core.Size;

public interface ILineDetectionModule extends IModule {
    void suscribe(ILineDetectionListener listener);
    void unsuscribe(ILineDetectionListener listener);
    void pauseDetection();
    void resumeDetection();

//    void useMask(boolean b);

    Size getMatSize();
}
