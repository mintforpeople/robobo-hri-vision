/*******************************************************************************
 *
 *   Copyright 2017 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2017 Luis Llamas <luis.llamas@mytechia.com>
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
package com.mytechia.robobo.framework.hri.vision.blobTracking;

import android.util.Log;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.util.AverageFilter;
import com.mytechia.robobo.framework.hri.vision.util.IFilter;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import java.util.Date;
import java.util.HashSet;


/**
 * Abstract class that manages the listener subscription, unsubscription and notifications
 */
public abstract class ABlobTrackingModule implements IBlobTrackingModule {
    private HashSet<IBlobListener> listeners;
    protected float resolutionX = 1;
    protected float resolutionY = 1;
    public ABlobTrackingModule(){
        listeners = new HashSet<IBlobListener>();
    }
    protected RoboboManager m;
    protected IRemoteControlModule rcmodule = null;

    protected Blobcolor RED_CAL = Blobcolor.RED;
    protected Blobcolor GREEN_CAL = Blobcolor.GREEN;
    protected Blobcolor BLUE_CAL = Blobcolor.BLUE;
    protected Blobcolor CUSTOM_CAL = Blobcolor.CUSTOM;

    private IFilter filterPosX = new AverageFilter(5);
    private IFilter filterPosY = new AverageFilter(5);
    private IFilter filterSize = new AverageFilter(5);

    /**
     * Called when a blob is detected
     * @param blob the detected blob
     */
    public void notifyTrackingBlob(Blob blob){
        for (IBlobListener listener:listeners){
            listener.onTrackingBlob(blob);
        }
        // Send status
        if (rcmodule!=null) {
            Log.d("BLOB","Notify blob");

            Status status = new Status("BLOB");
            status.putContents("posx",Math.round(((float)blob.getX()/resolutionX)*100)+"");
            status.putContents("posy",Math.round(((float)blob.getY()/resolutionY)*100)+"");
            status.putContents("size",blob.getSize()+"");
            status.putContents("color",colorToString(blob.getColor()));
            status.putContents("frame_id",blob.getFrameSequenceNumber()+"");

            status.putContents("frame_timestamp",blob.getFrameTimestamp()+"");
            rcmodule.postStatus(status);
        }
    }

    /**
     * Called when a blob disappears
     * @param c blob color
     */
    public void notifyBlobDissapear(Blobcolor c){

        for (IBlobListener listener:listeners){
            listener.onBlobDisappear(c);
        }
        if (rcmodule!=null) {
            Status status = new Status("BLOB");
            status.putContents("posx","0");
            status.putContents("posy","0");
            status.putContents("size","0");
            status.putContents("frame_id","0");
            status.putContents("color",c.name().toLowerCase());
            //Getting the current date
            Date date = new Date();
            //This method returns the time in millis
            long timeMilli = date.getTime();
            status.putContents("frame_timestamp", timeMilli+"");
            rcmodule.postStatus(status);
        }
    }

    /**
     * Gets a printable name from a blobcolor
     * @param blobcolor the color
     * @return a String with the color name
     */
    private String colorToString(Blobcolor blobcolor){
        if (blobcolor == BLUE_CAL){
            return "blue";
        }else if (blobcolor == GREEN_CAL){
            return  "green";
        }else if (blobcolor == RED_CAL){
            return "red";
        }else{
            return "custom";
        }

    }


    public void suscribe(IBlobListener listener){
        m.log("Blob_module", "Suscribed:"+listener.toString());
        listeners.add(listener);
    }
    public void unsuscribe(IBlobListener listener){
        listeners.remove(listener);
    }
}
