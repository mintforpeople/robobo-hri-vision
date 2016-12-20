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

package com.mytechia.robobo.framework.hri.vision.colorDetection;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Status;

import java.util.HashSet;


public abstract class AColorDetectionModule implements IColorDetectionModule {
    private HashSet<IColorListener> listeners = new HashSet<IColorListener>();
    protected IRemoteControlModule rcmodule = null;

    @Override
    public void suscribe(IColorListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unsuscribe(IColorListener listener) {
        listeners.remove(listener);
    }

//    protected void notifyColor(int colorrgb, int nearest_color, int x, int y, int height, int width, Bitmap borders){
//        for (IColorListener listener:listeners){
//            listener.onNewColor(colorrgb,nearest_color,x,y,height,width,borders);
//
//        }
//    }

    protected void notifyColor(int colorrgb, int nearest_color){
        for (IColorListener listener:listeners){
            listener.onNewColor(colorrgb,nearest_color);
        }
        if (rcmodule!=null) {
            Status status = new Status("NEWCOLOR");
            switch (nearest_color) {
                case Color.BLUE:
                    status.putContents("color", "blue");
                    break;
                case Color.CYAN:
                    status.putContents("color", "cyan");
                    break;
                case Color.MAGENTA:
                    status.putContents("color", "magenta");
                    break;
                case Color.GREEN:
                    status.putContents("color", "green");
                    break;
                case Color.YELLOW:
                    status.putContents("color", "yellow");
                    break;
                case Color.RED:
                    status.putContents("color", "red");
                    break;
            }
            rcmodule.postStatus(status);
        }
    }

}
