package com.mytechia.robobo.framework.hri.vision.laneDetection;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.lineDetection.ILineDetectionModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import org.opencv.core.Mat;

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

    protected void notifyLinesDetected(double slope_left, double bias_left, double slope_right, double bias_right) {
        for (ILaneDetectionListener listener : listeners) {
            listener.onLane(slope_left, bias_left, slope_right, bias_right);
        }
        if (rcmodule != null) {
            Status status = new Status("LANE_BASIC");
            status.putContents("a1", String.valueOf(slope_left));
            status.putContents("b1", String.valueOf(bias_left));
            status.putContents("a2", String.valueOf(slope_right));
            status.putContents("b2", String.valueOf(bias_right));
            status.putContents("id", counter + "");
            rcmodule.postStatus(status);
            counter++;
        }
    }

    protected void notifyLinesDetected(Line line_lt, Line line_rt, Mat minv, int frame_id) {
        double[] l = line_lt.lastFitPixel;
        double[] r = line_rt.lastFitPixel;
        if (l == null || r == null)
            return;
        for (ILaneDetectionListener listener : listeners) {
            listener.onLane(line_lt, line_rt, minv);
        }
        if (rcmodule != null) {
            Status status = new Status("LANE_PRO");

            status.putContents("left_a", String.valueOf(l[0]));
            status.putContents("left_b", String.valueOf(l[1]));
            status.putContents("left_c", String.valueOf(l[2]));
            status.putContents("right_a", String.valueOf(r[0]));
            status.putContents("right_b", String.valueOf(r[1]));
            status.putContents("right_c", String.valueOf(r[2]));
            status.putContents("minv", formatTransformationMatrix(minv));
            status.putContents("id", String.valueOf(frame_id));
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
                + "]" + ","
                + "[" +
                minv.get(2, 0)[0] + "," +
                minv.get(2, 1)[0] + "," +
                minv.get(2, 2)[0]
                + "]"
                + "]";
    }
}
