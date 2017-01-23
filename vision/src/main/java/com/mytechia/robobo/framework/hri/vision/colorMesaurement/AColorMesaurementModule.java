package com.mytechia.robobo.framework.hri.vision.colorMesaurement;

import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import java.util.HashSet;

/**
 * Created by luis on 19/1/17.
 */

public abstract class AColorMesaurementModule implements IColorMesaurementModule {
    private HashSet<IColorMesauredListener> listeners = new HashSet<>();

    protected IRemoteControlModule rcmodule = null;
    @Override
    public void suscribe(IColorMesauredListener listener){
        listeners.add(listener);
    }
    @Override
    public void unsuscribe(IColorMesauredListener listener){
        listeners.remove(listener);
    }

    protected void notifyColorMesaured(int r, int g, int b){
        for (IColorMesauredListener l:listeners) {
            l.onColorMesaured(r,g,b);
        }

        if (rcmodule!=null) {
            Status status = new Status("MEASUREDCOLOR");
            status.putContents("R",r+"");
            status.putContents("G",g+"");
            status.putContents("B",b+"");
        }

    }
}
