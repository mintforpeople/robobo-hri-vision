package com.mytechia.robobo.framework.hri.vision.cameraStream;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;

public abstract class ACameraStreamModule implements ICameraStreamModule {
    //private HashSet<ITagListener> listeners = new HashSet<ITagListener>();
    protected ICameraModule cameraModule;
    protected RoboboManager m;

}
