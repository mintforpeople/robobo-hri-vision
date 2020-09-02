package com.mytechia.robobo.framework.hri.vision.objectRecognition;

import com.google.gson.Gson;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import java.util.HashSet;
import java.util.List;

public abstract class AObjectRecognitionModule implements IObjectRecognitionModule {
    private HashSet<IObjectRecognizerListener> listeners = new HashSet<IObjectRecognizerListener>();
    protected IRemoteControlModule rcmodule = null;
    protected RoboboManager m;
    protected int imgWidth;
    protected int imgHeight;
    private boolean rosTypeStatus = false;

    @Override
    public void suscribe(IObjectRecognizerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(IObjectRecognizerListener listener) {
        listeners.remove(listener);
    }

    public void useRosTypeStatus(boolean use) {
        rosTypeStatus = use;
    }

    protected void notifyObjectDetected(List<RecognizedObject> detections, String frame_id) {
        for (IObjectRecognizerListener listener : listeners) {
            listener.onObjectsRecognized(detections);
        }
        if (rcmodule != null) {
            // For ros
            // Header comes from image
            // Object count

            if (rosTypeStatus) {
                Gson gson = new Gson();

                Status status = new Status("DETECTED_OBJECT");
                status.putContents("frame_id", frame_id);
                status.putContents("count", String.valueOf(detections.size()));
                status.putContents("detections", gson.toJson(detections));
                rcmodule.postStatus(status);
            } else
                for (RecognizedObject obj : detections) {
                    Status status = new Status("DETECTED_OBJECT");
                    status.putContents("id", String.valueOf(obj.getId()));
                    status.putContents("label", obj.getLabel());
                    status.putContents("posx", (int) obj.getBoundingBox().centerX() + "");
                    status.putContents("posy", (int) obj.getBoundingBox().centerY() + "");
                    status.putContents("width", (int) obj.getBoundingBox().width() + "");
                    status.putContents("height", (int) obj.getBoundingBox().height() + "");
                    status.putContents("confidence", obj.getConfidence() + "");
                    rcmodule.postStatus(status);

                }


        }
    }
}
