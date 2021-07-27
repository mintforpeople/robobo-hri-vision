package com.mytechia.robobo.framework.hri.vision.lineDetection;

import android.util.Log;

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
    protected boolean status=false;

    @Override
    public void suscribe(ILineDetectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(ILineDetectionListener listener) {
        listeners.remove(listener);
    }


    protected void notifyLinesDetected(Mat lines, int frame_id) {
        if(lines.rows()<=0)
            return;

        for (ILineDetectionListener listener : listeners) {
            listener.onLine(lines);
        }
        if (rcmodule != null && status) { // USE CAREFULLY, GENERATES A LOT OF LAN TRAFFIC!!!
            Log.d("LINE","Notify lines");

            Status status = new Status("LINE");

            status.putContents("mat", formatLines(lines));
            status.putContents("id", String.valueOf(frame_id));
            rcmodule.postStatus(status);

        }
    }

    private String formatLines(Mat lines) {
        StringBuilder res= new StringBuilder("[");
        for (int i = 0; i < lines.rows(); i++) {
            double[] l = lines.get(i, 0);

            res.append("[");
            res.append((int) l[0]);
            res.append(", ");
            res.append((int) l[1]);
            res.append(", ");
            res.append((int) l[2]);
            res.append(", ");
            res.append((int) l[3]);
            res.append("]");
            if (i!=lines.rows()-1)
                res.append(", ");
        }
        res.append("]");
        return res.toString();
    }

}
