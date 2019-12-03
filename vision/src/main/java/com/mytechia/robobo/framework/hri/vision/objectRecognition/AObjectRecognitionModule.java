package com.mytechia.robobo.framework.hri.vision.objectRecognition;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import java.util.HashSet;
import java.util.List;

public abstract class AObjectRecognitionModule implements IObjectRecognitionModule{
    private HashSet<IObjectRecognizerListener> listeners = new HashSet<IObjectRecognizerListener>();
    protected IRemoteControlModule rcmodule = null;
    protected RoboboManager m;

    @Override
    public void suscribe(IObjectRecognizerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(IObjectRecognizerListener listener) {
        listeners.remove(listener);
    }


    protected void notifyObjectDetected(List<RecognizedObject> detections){
        for (IObjectRecognizerListener listener:listeners){
            listener.onObjectsRecognized(detections);
        }
        if (rcmodule!=null) {

            for (RecognizedObject obj: detections){
                Status status = new Status("OBJECT");
                status.putContents("label",obj.getLabel());
                status.putContents("posx", (int)obj.getBoundingBox().centerX() + "");
                status.putContents("posy", (int)obj.getBoundingBox().centerY() + "");
                status.putContents("confidence", obj.getConfidence() + "");
                rcmodule.postStatus(status);

            }
        }
    }
}
