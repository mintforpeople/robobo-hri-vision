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

import com.mytechia.robobo.framework.RoboboManager;

import org.opencv.core.Mat;

import java.util.Date;
import java.util.HashSet;

/**
 * Abstract class that manages listeners and status
 */
public abstract class ACameraModule implements ICameraModule{

    private final HashSet<ICameraListener> listeners ;
    private final HashSet<ICameraListenerV2> listenersV2;

    protected RoboboManager roboboManager;

    public ACameraModule(){
        listeners = new HashSet<>();
        listenersV2 = new HashSet<>();
    }

    /**
     * To notify when a frame is captured
     * @param frame the captured frame
     */
    protected void notifyFrame(Frame frame){
        synchronized (listeners) {
            for (ICameraListener listener : listeners) {
                listener.onNewFrame(frame);
            }
        }
    }

    /**
     * To notify when a frame is captured in debug mode
     * @param frame the captured frame
     * @param id identifier to filter frames
     */
    protected void notifyDebugFrame(Frame frame, String id){
        synchronized (listeners) {
            for (ICameraListener listener : listeners) {
                listener.onDebugFrame(frame, id);
            }
        }
    }
    /**
     * To notify when a frame is captured
     * @param mat the captured frame
     */
    protected void notifyMat(Mat mat){
        synchronized (listeners) {
            for (ICameraListener listener : listeners) {
                listener.onNewMat(mat.clone());
            }
        }
    }

    /**
     * To notify when a frame is captured
     * @param mat the captured frame in opencv mat
     * @param seqnum sequence number
     */
    protected void notifyMat(Mat mat, int seqnum, long timestamp) {

        synchronized (listenersV2){
            for (ICameraListenerV2 listener :
                    listenersV2) {
                listener.onNewMatV2(mat.clone(), seqnum,timestamp);
            }
        }
    }

    /**
     * To notify when the opencv library is loaded
     *
     */
    protected void notifyOpenCVStartup(){
        synchronized (listeners) {
            for (ICameraListener listener : listeners) {
                listener.onOpenCVStartup();
            }
        }
        synchronized (listenersV2){
            for (ICameraListenerV2 listener:
                    listenersV2) {
                listener.onOpenCVStartup();
            }
        }
    }

    public void suscribe(ICameraListener listener){
        synchronized (listeners) {
            roboboManager.log("Cam_module", "Suscribed:" + listener.toString());
            listeners.add(listener);
        }
    }
    public void unsuscribe(ICameraListener listener){
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    public void suscribe(ICameraListenerV2 listener){
        synchronized (listenersV2) {
            roboboManager.log("Cam_module", "Suscribed:" + listener.toString());
            listenersV2.add(listener);
        }
    }
    public void unsuscribe(ICameraListenerV2 listener){
        synchronized (listenersV2) {
            listenersV2.remove(listener);
        }
    }
}
