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
package com.mytechia.robobo.framework.hri.vision.faceDetection;

import android.graphics.PointF;
import android.util.Log;

import com.mytechia.robobo.framework.IModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import org.opencv.core.Mat;

import java.util.HashSet;

/**
 * Abstract class that manages the listeners and the status
 */
public abstract class AFaceDetectionModule implements IFaceDetectionModule{
    private HashSet<IFaceListener> listeners;
    protected IRemoteControlModule rcmodule = null;
    public AFaceDetectionModule(){
        listeners = new HashSet<IFaceListener>();
    }

    /**
     * Notifies the listeners when a face is detected
     * @param coords The coordinates of the center of the face
     * @param eyesDistance The distance between eyes
     */
    protected void notifyFace(PointF coords, float eyesDistance){
        for (IFaceListener listener:listeners){
            listener.onFaceDetected(coords,eyesDistance);
        }
        if (rcmodule!=null) {
            Status status = new Status("NEWFACE");
            status.putContents("coordx",Math.round(coords.x)+"");
            status.putContents("coordy",Math.round(coords.y)+"");
            status.putContents("distance", Math.round(eyesDistance)+"");

            rcmodule.postStatus(status);
        }
    }

    protected void notifyFaceAppear(PointF coords, float eyesDistance){
        for (IFaceListener listener:listeners){
            listener.onFaceAppear(coords,eyesDistance);
        }
        if (rcmodule!=null) {
            Status status = new Status("FOUNDFACE");
            status.putContents("coordx",Math.round(coords.x)+"");
            status.putContents("coordy",Math.round(coords.y)+"");
            status.putContents("distance", Math.round(eyesDistance)+"");

            rcmodule.postStatus(status);
        }
    }

    protected void notifyFaceDisappear(){
        for (IFaceListener listener:listeners){
            listener.onFaceDissapear();
        }
        if (rcmodule!=null) {
            Status status = new Status("LOSTFACE");

            rcmodule.postStatus(status);
        }
    }

    public void suscribe(IFaceListener listener){
        Log.d("FD_module", "Suscribed:"+listener.toString());
        listeners.add(listener);
    }
    public void unsuscribe(IFaceListener listener){
        listeners.remove(listener);
    }
}
