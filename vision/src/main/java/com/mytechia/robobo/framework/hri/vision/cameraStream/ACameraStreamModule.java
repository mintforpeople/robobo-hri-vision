package com.mytechia.robobo.framework.hri.vision.cameraStream;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import java.util.HashSet;
import java.util.List;

public abstract class ACameraStreamModule implements ICameraStreamModule {
    //private HashSet<ITagListener> listeners = new HashSet<ITagListener>();
    protected IRemoteControlModule rcmodule = null;
    protected RoboboManager m;

//    @Override
//    public void suscribe(ITagListener listener) {
//        listeners.add(listener);
//    }
//
//    @Override
//    public void unsuscribe(ITagListener listener) {
//        listeners.remove(listener);
//    }
//

    /*protected void notifyMarkersDetected(List<Mat> corners, Mat ids){
        for (ITagListener listener:listeners){
            listener.onAruco(corners, ids);
        }
        if (rcmodule!=null) {

            /*for (RecognizedObject obj: detections){
                Status status = new Status("OBJECT");
                status.putContents("label",obj.getLabel());
                status.putContents("posx", (int)obj.getBoundingBox().centerX() + "");
                status.putContents("posy", (int)obj.getBoundingBox().centerY() + "");
                status.putContents("confidence", obj.getConfidence() + "");
                rcmodule.postStatus(status);

            }
        }
    }*/



}
