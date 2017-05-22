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

import com.mytechia.robobo.framework.RoboboManager;

import java.util.HashSet;

public abstract class ABlobTrackingModule implements IBlobTrackingModule {
    private HashSet<IBlobListener> listeners;

    public ABlobTrackingModule(){
        listeners = new HashSet<IBlobListener>();
    }
    protected RoboboManager m;

    protected void notifyTrackingBlob(Blob blob){
        for (IBlobListener listener:listeners){
            listener.onTrackingBlob(blob);
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
