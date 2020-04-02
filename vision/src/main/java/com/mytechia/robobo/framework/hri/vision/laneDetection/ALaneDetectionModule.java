package com.mytechia.robobo.framework.hri.vision.laneDetection;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.lineDetection.ILineDetectionModule;
import com.mytechia.robobo.framework.hri.vision.util.GsonConverter;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.HashSet;

public abstract class ALaneDetectionModule implements ILaneDetectionModule {
    protected HashSet<ILaneDetectionListener> listeners = new HashSet<ILaneDetectionListener>();
    protected IRemoteControlModule rcmodule = null;
    protected ILineDetectionModule lineModule = null;
    protected RoboboManager m;
    private int counter = 0;

    @Override
    public void suscribe(ILaneDetectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(ILaneDetectionListener listener) {
        listeners.remove(listener);
    }

    protected void notifyLinesDetected(double a1, double b1, double a2, double b2) {
        for (ILaneDetectionListener listener : listeners) {
            listener.onLane(a1, b1, a2, b2);
        }
        if (rcmodule != null) {
            Status status = new Status("LANE_BASIC");
            status.putContents("a1", a1 + "");
            status.putContents("b1", a2 + "");
            status.putContents("a2", b1 + "");
            status.putContents("b2", b2 + "");
            status.putContents("id", counter + "");
            rcmodule.postStatus(status);
            counter++;
        }
    }

    protected void notifyLinesDetected(Line line_lt, Line line_rt, Mat minv) {
        double[] l = line_lt.last_fit_pixel;
        double[] r = line_rt.last_fit_pixel;
        if (l == null || r == null)
            return;
        for (ILaneDetectionListener listener : listeners) {
            listener.onLane(line_lt, line_rt, minv);
        }
        if (rcmodule != null) {
            Status status = new Status("LANE_PRO");

            status.putContents("left_a", l[0] + "");
            status.putContents("left_b", l[1] + "");
            status.putContents("left_c", l[2] + "");
            status.putContents("right_a", r[0] + "");
            status.putContents("right_b", r[1] + "");
            status.putContents("right_c", r[2] + "");
            status.putContents("minv", formatTransformationMatrix(minv) + "");
            status.putContents("id", counter + "");
            rcmodule.postStatus(status);
            counter++;
        }
    }

    private String formatTransformationMatrix(Mat minv) {
        return "[" +
                "[" +
                minv.get(0, 0)[0] + "," +
                minv.get(0, 1)[0] + "," +
                minv.get(0, 2)[0]
                + "]" + ","
                + "[" +
                minv.get(1, 0)[0] + "," +
                minv.get(1, 1)[0] + "," +
                minv.get(1, 2)[0]
                + "]"+ ","
                + "[" +
                minv.get(2, 0)[0] + "," +
                minv.get(2, 1)[0] + "," +
                minv.get(2, 2)[0]
                + "]"
                + "]";
    }
}
