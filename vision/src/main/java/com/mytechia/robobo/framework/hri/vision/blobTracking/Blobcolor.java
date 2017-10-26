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


 import com.mytechia.robobo.framework.hri.vision.util.CameraCalibrationData;
 import com.mytechia.robobo.framework.hri.vision.util.ColorCalibrationData;

 import org.opencv.core.Scalar;

/**
 * Defines the detectable colors ad its HSV ranges
 */
public class Blobcolor {

    public static Blobcolor GREEN = blobColorFromColorCalibrationData(CameraCalibrationData.DEFAULT_GREEN,"GREEN");
    public static Blobcolor BLUE = blobColorFromColorCalibrationData(CameraCalibrationData.DEFAULT_BLUE,"BLUE");
    public static Blobcolor RED = blobColorFromColorCalibrationData(CameraCalibrationData.DEFAULT_RED,"RED");
    public static Blobcolor CUSTOM = blobColorFromColorCalibrationData(CameraCalibrationData.DEFAULT_CUSTOM,"CUSTOM");

    private int hmin;
    private int smin;
    private int vmin;
    private int hmax;
    private int smax;
    private int vmax;
    private String name;



    public Blobcolor(int hmin, int smin, int vmin, int hmax, int smax, int vmax, String name){

        this.hmax = hmax;
        this.smax = smax;
        this.vmax = vmax;
        this.hmin = hmin;
        this.smin = smin;
        this.vmin = vmin;
        this.name = name;
    }

    public static Scalar getLowRange(Blobcolor color){
        return new Scalar(color.hmin, color.smin, color.vmin);
    }

    public static Scalar getHighRange(Blobcolor color){
        return new Scalar(color.hmax, color.smax, color.vmax);
    }

    public static Blobcolor blobColorFromColorCalibrationData(ColorCalibrationData color, String key) {
        return new Blobcolor (
                color.getMinH(),
                color.getMinS(),
                color.getMinV(),
                color.getMaxH(),
                color.getMaxS(),
                color.getMaxV(),
                key);
    }

    public String name() {
        return name;
    }

}
