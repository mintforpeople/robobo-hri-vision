package com.mytechia.robobo.framework.hri.vision.aruco;

import com.mytechia.robobo.framework.IModule;

public interface IArucoModule extends IModule {
    void suscribe(IArucoListener listener);
    void unsuscribe(IArucoListener listener);
}
