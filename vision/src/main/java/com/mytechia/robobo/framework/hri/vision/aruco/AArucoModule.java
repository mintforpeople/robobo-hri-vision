package com.mytechia.robobo.framework.hri.vision.aruco;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.core.Mat;

import java.util.HashSet;
import java.util.List;

public abstract class AArucoModule implements IArucoModule {
    private HashSet<IArucoListener> listeners = new HashSet<IArucoListener>();
    protected IRemoteControlModule rcmodule = null;
    protected RoboboManager m;

    @Override
    public void suscribe(IArucoListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(IArucoListener listener) {
        listeners.remove(listener);
    }


    protected void notifyMarkersDetected(List<Mat> corners, Mat ids){
        for (IArucoListener listener:listeners){
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

            }*/
        }
    }

}
