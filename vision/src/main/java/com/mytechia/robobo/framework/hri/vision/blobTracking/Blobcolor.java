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


 import org.opencv.core.Scalar;

/**
 * Defines the detectable colors ad its HSV ranges
 */
public class Blobcolor {

//    Originals
//    GREEN(29, 170, 100, 77, 255, 255),
//    BLUE(0,80,40,20,255,200),
//    RED(90,150,100,179,255,240);

    //BQ
//    GREEN(39,187,87,67,255,233),
//    BLUE(11,140,127,13,255,199),
//    RED(72,114,199,179,210,255),
//    CUSTOM(45,98,148,135,226,255);
      //Samsung
    public static Blobcolor GREEN = new Blobcolor(46,100 ,127,68,206,221,"GREEN");
    public static Blobcolor BLUE = new Blobcolor (13,133,121,19,255,209,"BLUE");
    public static Blobcolor RED = new Blobcolor(82,102,184,164,190,246,"RED");
    public static Blobcolor CUSTOM = new Blobcolor(47,96,110,137,255,218,"CUSTOM");

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

    public String name() {
        return name;
    }

}
