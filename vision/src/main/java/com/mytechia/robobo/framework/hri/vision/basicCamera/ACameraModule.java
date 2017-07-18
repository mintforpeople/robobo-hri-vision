/*******************************************************************************
 *
 *   Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2016 Luis Llamas <luis.llamas@mytechia.com>
 *
 *   This file is part of Robobo HRI Modules.
 *
 *   Robobo HRI Modules is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo HRI Modules is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo HRI Modules.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package com.mytechia.robobo.framework.hri.vision.basicCamera;

import android.util.Log;

import com.mytechia.robobo.framework.RoboboManager;

import org.opencv.core.Mat;

import java.util.HashSet;

/**
 * Abstract class that manages listeners and status
 */
public abstract class ACameraModule implements ICameraModule{
    private HashSet<ICameraListener> listeners ;
    public ACameraModule(){
        listeners = new HashSet<ICameraListener>();
    }
    protected RoboboManager m;
    protected void notifyFrame(Frame frame){
        for (ICameraListener listener:listeners){
                listener.onNewFrame(frame);
        }
    }
    protected void notifyDebugFrame(Frame frame, String id){
        for (ICameraListener listener:listeners){
                listener.onDebugFrame(frame, id);
        }
    }

    protected void notifyMat(Mat mat){
        for (ICameraListener listener:listeners){
            listener.onNewMat(mat);
        }
    }

    public void suscribe(ICameraListener listener){
        m.log("Cam_module", "Suscribed:"+listener.toString());
        listeners.add(listener);
    }
    public void unsuscribe(ICameraListener listener){
        listeners.remove(listener);
    }
}
