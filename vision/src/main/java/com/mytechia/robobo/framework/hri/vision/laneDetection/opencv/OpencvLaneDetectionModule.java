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
        double slope1_avg = 0,
                slope2_avg = 0,
                yinter1 = 0,
                yinter2 = 0,
                count1 = 0,
                count2 = 0;
        int y1, y2;
        Mat res;
        for (int x = 0; x < lines.rows(); x++) {
            // x1, y1, x2, y2
            double[] l = lines.get(x, 0);

            double slope = (l[3] - l[1]) / (l[2] - l[0]);
            double y_intercept = l[1] - slope * l[0];

            if (slope < 0) {
                slope1_avg += slope;
                yinter1 += y_intercept;
                count1++;
            } else {
                slope2_avg += slope;
                yinter2 += y_intercept;
                count2++;
            }
        }
        if (count1==0||count2==0)
            return;
        else
            res = new Mat(2, 4, lines.type());


        slope1_avg /= count1;
        yinter1 /= count1;
        slope2_avg /= count2;
        yinter2 /= count2;

        // Bottom of the line
        y1 = (int) lineModule.getMatSize().height;

        // Top of the line
        y2 = (int) (y1 - (y1 * 0.3125));

        //Todo: move this to other function

        res.put(0, 0, (int) ((y1 - yinter1) / slope1_avg), y1, (int) ((y2 - yinter1) / slope1_avg), y2);
        res.put(1, 0, (int) ((y1 - yinter2) / slope2_avg), y1, (int) ((y2 - yinter2) / slope2_avg), y2);

        notifyLinesDetected( res);
    }
}