package com.mytechia.robobo.framework.hri.vision.blobTracking;

/*******************************************************************************
 * Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 * Copyright 2016 Luis Llamas <luis.llamas@mytechia.com>
 * <p>
 * This file is part of Robobo Remote Control Module.
 * <p>
 * Robobo Remote Control Module is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Robobo Remote Control Module is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Robobo Remote Control Module.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

import org.opencv.core.Scalar;

/**
 * Defines the detectable colors ad its HSV ranges
 */
public enum Blobcolor {

//    GREEN(29, 86, 6, 64, 255, 255),
//   bueno  GREEN(29, 75, 6, 77, 255, 255),
//    last GREEN(29, 170, 100, 77, 255, 255),
    GREEN(49, 58, 112, 61, 222, 210),
    BLUE(0,80,40,20,255,200),
//    BLUE(0,80,40,20,255,150),
    RED(90,150,100,179,255,240);
//    RED(100,150,100,179,255,200);

    public int hmin;
    public int smin;
    public int vmin;
    public int hmax;
    public int smax;
    public int vmax;

    Blobcolor(int hmin, int smin, int vmin, int hmax, int smax, int vmax){

        this.hmax = hmax;
        this.smax = smax;
        this.vmax = vmax;
        this.hmin = hmin;
        this.smin = smin;
        this.vmin = vmin;
    }

    public static Scalar getLowRange(Blobcolor color){
        return new Scalar(color.hmin, color.smin, color.vmin);
    }

    public static Scalar getHighRange(Blobcolor color){
        return new Scalar(color.hmax, color.smax, color.vmax);
    }
}
