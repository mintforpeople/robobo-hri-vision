package com.mytechia.robobo.framework.hri.vision.laneDetection.opencv;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.laneDetection.ALaneDetectionModule;
import com.mytechia.robobo.framework.hri.vision.lineDetection.ILineDetectionListener;
import com.mytechia.robobo.framework.hri.vision.lineDetection.ILineDetectionModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.core.Mat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpencvLaneDetectionModule extends ALaneDetectionModule implements ILineDetectionListener {

    private boolean processing = false, useMask = false;

    private ExecutorService executor;

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {


        m = manager;
        // Load camera and remote control modules
        try {
            lineModule = m.getModuleInstance(ILineDetectionModule.class);
            rcmodule = m.getModuleInstance(IRemoteControlModule.class);


        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }

        executor = Executors.newFixedThreadPool(1);

        rcmodule.registerCommand("START-LANE", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                startDetection();
            }
        });

        rcmodule.registerCommand("STOP-LANE", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                stopDetection();

            }
        });

        startDetection();

    }

    private void stopDetection() {
        lineModule.unsuscribe(this);
    }

    private void startDetection() {
        lineModule.suscribe(this);
    }

    @Override
    public void shutdown() throws InternalErrorException {
        stopDetection();
    }

    @Override
    public String getModuleInfo() {
        return null;
    }

    @Override
    public String getModuleVersion() {
        return null;
    }




    @Override
    public void pauseDetection() {
        lineModule.unsuscribe(this);
    }

    @Override
    public void resumeDetection() {
        lineModule.suscribe(this);
    }

    @Override
    public void onLine(Mat lines) {
        double slope_left_avg = 0,
                slope_right_avg = 0,
                y_intercept_left = 0,
                y_intercept_right = 0,
                count_left = 0,
                count_right = 0;
        int y1, y2;
        Mat res;
        for (int x = 0; x < lines.rows(); x++) {
            // x1, y1, x2, y2
            double[] l = lines.get(x, 0);

            double slope = (l[3] - l[1]) / (l[2] - l[0]);
            double y_intercept = l[1] - slope * l[0];

            if (slope < 0) {
                slope_left_avg += slope;
                y_intercept_left += y_intercept;
                count_left++;
            } else {
                slope_right_avg += slope;
                y_intercept_right += y_intercept;
                count_right++;
            }
        }
        if (count_left==0||count_right==0)
            return;
//        else
//            res = new Mat(2, 4, lines.type());


        slope_left_avg /= count_left;
        y_intercept_left /= count_left;
        slope_right_avg /= count_right;
        y_intercept_right /= count_right;

//        // Bottom of the line
//        y1 = (int) lineModule.getMatSize().height;
//
//        // Top of the line
//        y2 = (int) (y1 - (y1 * 0.3125));
//
//        res.put(0, 0, (int) ((y1 - y_intercept_left) / slope_left_avg), y1, (int) ((y2 - y_intercept_left) / slope_left_avg), y2);
//        res.put(1, 0, (int) ((y1 - y_intercept_right) / slope_right_avg), y1, (int) ((y2 - y_intercept_right) / slope_right_avg), y2);
//        res.put(0, 0, (int) ((y1 - y_intercept_left) / slope_left_avg), y1, (int) ((y2 - y_intercept_left) / slope_left_avg), y2);
//        res.put(1, 0, (int) ((y1 - y_intercept_right) / slope_right_avg), y1, (int) ((y2 - y_intercept_right) / slope_right_avg), y2);

        notifyLinesDetected( slope_left_avg, y_intercept_left, slope_right_avg, y_intercept_right);
    }
}