package com.mytechia.robobo.framework.hri.vision.tag;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import java.util.HashSet;
import java.util.List;

public abstract class ATagModule implements ITagModule {
    private HashSet<ITagListener> listeners = new HashSet<ITagListener>();
    protected IRemoteControlModule rcmodule = null;
    protected RoboboManager m;
    private boolean rosTypeStatus = false;

    @Override
    public void suscribe(ITagListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(ITagListener listener) {
        listeners.remove(listener);
    }

    public void useRosTypeStatus(boolean use) {
        rosTypeStatus = use;
    }
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

    protected void notifyMarkersDetected(List<Tag> tags, int frame_id) {
        for (ITagListener listener : listeners) {
            listener.onAruco(tags);
        }
        if (rcmodule != null) {
            //TODO: confirmar que si se quiere modificado y nos dos por aparte
            for (Tag tag : tags) {
                Status status = new Status("TAG");
                status.putContents("frame_id", String.valueOf(frame_id));
                status.putContents("id", tag.getId() + "");
                if (rosTypeStatus) {
                    double[] quaternion = tag.getQuaternion();
                    status.putContents("quaternion_0", String.valueOf(quaternion[0]));
                    status.putContents("quaternion_1", String.valueOf(quaternion[1]));
                    status.putContents("quaternion_2", String.valueOf(quaternion[2]));
                    status.putContents("quaternion_3", String.valueOf(quaternion[3]));

                } else {
                    status.putContents("cor1x", String.valueOf((int) tag.getCorner(0).x));
                    status.putContents("cor1y", String.valueOf((int) tag.getCorner(0).y));
                    status.putContents("cor2x", String.valueOf((int) tag.getCorner(1).x));
                    status.putContents("cor2y", String.valueOf((int) tag.getCorner(1).y));
                    status.putContents("cor3x", String.valueOf((int) tag.getCorner(2).x));
                    status.putContents("cor3y", String.valueOf((int) tag.getCorner(2).y));
                    status.putContents("cor4x", String.valueOf((int) tag.getCorner(3).x));
                    status.putContents("cor4y", String.valueOf((int) tag.getCorner(3).y));
                    status.putContents("rvec_0", String.valueOf(tag.getRvecs()[0]));
                    status.putContents("rvec_1", String.valueOf(tag.getRvecs()[1]));
                    status.putContents("rvec_2", String.valueOf(tag.getRvecs()[2]));

                }
                status.putContents("tvec_0", String.valueOf(tag.getTvecs()[0]));
                status.putContents("tvec_1", String.valueOf(tag.getTvecs()[1]));
                status.putContents("tvec_2", String.valueOf(tag.getTvecs()[2]));

                rcmodule.postStatus(status);

            }
        }
    }

}
