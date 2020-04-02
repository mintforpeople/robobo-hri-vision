package com.mytechia.robobo.framework.hri.vision.lineDetection;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import org.opencv.core.Mat;

import java.util.HashSet;

public abstract class ALineDetectionModule implements ILineDetectionModule {
    private HashSet<ILineDetectionListener> listeners = new HashSet<ILineDetectionListener>();
    protected IRemoteControlModule rcmodule = null;
    protected RoboboManager m;
    protected boolean status = false;

    @Override
    public void suscribe(ILineDetectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(ILineDetectionListener listener) {
        listeners.remove(listener);
    }


    protected void notifyLinesDetected(Mat lines) {
        for (ILineDetectionListener listener : listeners) {
            listener.onLine(lines);
        }
        if (rcmodule != null && status) { // USE CAREFULLY, GENERATES A LOT OF LAN TRAFFIC!!!
            for (int x = 0; x < lines.rows(); x++) {
                double[] l = lines.get(x, 0);

                Status status = new Status("LINE");
                status.putContents("cor1x", (int) l[0] + "");
                status.putContents("cor1y", (int) l[1] + "");
                status.putContents("cor2x", (int) l[2] + "");
                status.putContents("cor2y", (int) l[3] + "");
                rcmodule.postStatus(status);

            }
        }
    }

}
