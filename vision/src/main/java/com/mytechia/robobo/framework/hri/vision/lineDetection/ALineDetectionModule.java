package com.mytechia.robobo.framework.hri.vision.lineDetection;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.util.GsonConverter;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import org.opencv.core.Mat;

import java.util.HashSet;

public abstract class ALineDetectionModule implements ILineDetectionModule {
    private HashSet<ILineDetectionListener> listeners = new HashSet<ILineDetectionListener>();
    protected IRemoteControlModule rcmodule = null;
    protected RoboboManager m;
    private int counter;
    protected boolean status=false;

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
            Status status = new Status("LINE");

            status.putContents("mat", formatLines(lines));
            status.putContents("id", counter++ +"");
            rcmodule.postStatus(status);

        }
    }

    private String formatLines(Mat lines) {
        StringBuilder res= new StringBuilder("[");
        for (int i = 0; i < lines.rows(); i++) {
            double[] l = lines.get(i, 0);

            res.append("[");
            res.append(l[0]);
            res.append(",");
            res.append(l[1]);
            res.append(",");
            res.append(l[2]);
            res.append(",");
            res.append(l[3]);
            res.append("]");
            if (i!=lines.rows()-1)
                res.append(",");
        }
        res.append("]");
        return res.toString();
    }

}
