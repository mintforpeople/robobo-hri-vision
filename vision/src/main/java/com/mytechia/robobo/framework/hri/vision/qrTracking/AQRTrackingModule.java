/*******************************************************************************
 *
 *   Copyright 2018 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2018 Luis Llamas <luis.llamas@mytechia.com>
 *
 *   This file is part of Robobo Vision Modules.
 *
 *   Robobo Vision Modules is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo Remote Control Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo Vision Modules.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package com.mytechia.robobo.framework.hri.vision.qrTracking;

import android.util.Log;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import java.util.HashSet;


public abstract class AQRTrackingModule implements IQRTrackingModule {
    private HashSet<IQRListener> listeners = new HashSet<>();
    protected IRemoteControlModule rcmodule = null;
    protected RoboboManager m;
    private String TAG = "AQRModule";


    protected void notifyQR(QRInfo qr){
        for (IQRListener listener:listeners){
            listener.onQRDetected(qr);
        }
        if (rcmodule!=null) {
            Status status = new Status("QRCODE");
            status.putContents("coordx",qr.getxPosition()+"");
            status.putContents("coordy",qr.getyPosition()+"");
            status.putContents("id", qr.getIdString());
            status.putContents("distance", qr.getDistance()+"");
            status.putContents("p1x",qr.getRp1().getX()+"");
            status.putContents("p1y",qr.getRp1().getY()+"");
            status.putContents("p2x",qr.getRp2().getX()+"");
            status.putContents("p2y",qr.getRp2().getY()+"");
            status.putContents("p3x",qr.getRp3().getX()+"");
            status.putContents("p3y",qr.getRp3().getY()+"");
            rcmodule.postStatus(status);
        }
    }

    protected void notifyQRAppear(QRInfo qr) {
        Log.d(TAG,"QR Appeared");
        for (IQRListener listener : listeners) {
            listener.onQRAppears(qr);
        }
        if (rcmodule!=null) {
            Status status = new Status("QRCODEAPPEAR");
            status.putContents("coordx",qr.getxPosition()+"");
            status.putContents("coordy",qr.getyPosition()+"");
            status.putContents("distance", qr.getDistance()+"");

            status.putContents("id", qr.getIdString());

            rcmodule.postStatus(status);
        }
    }

    protected void notifyQRDisappear(QRInfo qr) {
        Log.d(TAG,"QR Disappeared");

        for (IQRListener listener : listeners) {
            listener.onQRDisappears(qr);
        }
        if (rcmodule!=null) {
            Status status = new Status("QRCODELOST");
            status.putContents("id", qr.getIdString());

            rcmodule.postStatus(status);
        }
    }




    public void suscribe(IQRListener listener){
        m.log("QR_module", "Suscribed:"+listener.toString());
        listeners.add(listener);
    }
    public void unsuscribe(IQRListener listener){
        listeners.remove(listener);
    }
}
