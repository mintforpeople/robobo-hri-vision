package com.mytechia.robobo.framework.hri.vision.laneDetection;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.lineDetection.ILineDetectionModule;
import com.mytechia.robobo.framework.hri.vision.util.GsonConverter;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import org.opencv.core.Mat;

import java.util.HashSet;

public abstract class ALaneDetectionModule implements ILaneDetectionModule {
    protected HashSet<ILaneDetectionListener> listeners = new HashSet<ILaneDetectionListener>();
    protected IRemoteControlModule rcmodule = null;
    protected ILineDetectionModule lineModule = null;
    protected RoboboManager m;

    @Override
    public void suscribe(ILaneDetectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(ILaneDetectionListener listener) {
        listeners.remove(listener);
    }

    protected void notifyLinesDetected(Mat lines){
        for (ILaneDetectionListener listener:listeners){
            listener.onLane(lines);
        }
        if (rcmodule != null) {
            for (int x = 0; x < lines.rows(); x++) {
                double[] l = lines.get(x, 0);

                Status status = new Status("LANE");
                status.putContents("cor1x", (int) l[0] + "");
                status.putContents("cor1y", (int) l[1] + "");
                status.putContents("cor2x", (int) l[2] + "");
                status.putContents("cor2y", (int) l[3] + "");
                rcmodule.postStatus(status);

            }
        }
    }
    protected void notifyLinesDetected(Line line_lt, Line line_rt, Mat minv){
        double[] l = line_lt.last_fit_pixel;
        double[] r = line_rt.last_fit_pixel;
        if(l==null || r==null)
            return;
        for (ILaneDetectionListener listener:listeners){
            listener.onLane(line_lt, line_rt, minv);
        }
        if (rcmodule != null) {
            Status status = new Status("LANE");

            status.putContents("left_a", (int) l[0] + "");
            status.putContents("left_b", (int) l[1] + "");
            status.putContents("left_c", (int) l[2] + "");
            status.putContents("right_a", (int) r[0] + "");
            status.putContents("right_b", (int) r[1] + "");
            status.putContents("right_c", (int) r[2] + "");
            status.putContents("min", GsonConverter.matToJson(minv) + "");
            rcmodule.postStatus(status);
        }
    }
}
