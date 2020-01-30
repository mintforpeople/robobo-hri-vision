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

    @Override
    public void suscribe(ITagListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(ITagListener listener) {
        listeners.remove(listener);
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

    protected void notifyMarkersDetected(List<Tag> tags){
        for (ITagListener listener:listeners){
            listener.onAruco(tags);
        }
        if (rcmodule!=null) {

            for (Tag tag: tags){
                Status status = new Status("TAG");
                status.putContents("id",tag.getId()+"");
                status.putContents("cor1x", (int)tag.getCorner(0).x + "");
                status.putContents("cor1y", (int)tag.getCorner(0).y + "");
                status.putContents("cor2x", (int)tag.getCorner(1).x + "");
                status.putContents("cor2y", (int)tag.getCorner(1).y + "");
                status.putContents("cor3x", (int)tag.getCorner(2).x + "");
                status.putContents("cor3y", (int)tag.getCorner(2).y + "");
                status.putContents("cor4x", (int)tag.getCorner(3).x + "");
                status.putContents("cor4y", (int)tag.getCorner(3).y + "");
                status.putContents("rvec_0", tag.getRvecs()[0]+"");
                status.putContents("rvec_1", tag.getRvecs()[1]+"");
                status.putContents("rvec_2", tag.getRvecs()[2]+"");
                status.putContents("tvec_0", tag.getTvecs()[0]+"");
                status.putContents("tvec_1", tag.getTvecs()[1]+"");
                status.putContents("tvec_2", tag.getTvecs()[2]+"");

                rcmodule.postStatus(status);

            }
        }
    }

}
