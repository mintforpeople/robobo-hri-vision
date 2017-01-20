package com.mytechia.robobo.framework.hri.vision.colorMesaurement;

import java.util.HashSet;

/**
 * Created by luis on 19/1/17.
 */

public abstract class AColorMesaurementModule implements IColorMesaurementModule {
    private HashSet<IColorMesauredListener> listeners = new HashSet<>();


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
    }
}
