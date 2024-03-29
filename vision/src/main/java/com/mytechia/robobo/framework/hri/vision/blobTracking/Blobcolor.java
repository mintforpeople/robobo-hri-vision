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
 import com.mytechia.robobo.framework.hri.vision.util.ColorCalibrationDataHSV;
 import com.mytechia.robobo.framework.hri.vision.util.ColorCalibrationDataHistogram;
 import com.mytechia.robobo.framework.hri.vision.util.IColorCalibrationData;

 import org.opencv.core.Mat;
 import org.opencv.core.Scalar;

/**
 * Defines the detectable colors ad its HSV ranges
 */
public class Blobcolor {

    public static Blobcolor GREEN = blobColorFromColorCalibrationData(CameraCalibrationData.DEFAULT_GREEN,"GREEN");
    public static Blobcolor BLUE = blobColorFromColorCalibrationData(CameraCalibrationData.DEFAULT_BLUE,"BLUE");
    public static Blobcolor RED = blobColorFromColorCalibrationData(CameraCalibrationData.DEFAULT_RED,"RED");
    public static Blobcolor CUSTOM = blobColorFromColorCalibrationData(CameraCalibrationData.DEFAULT_CUSTOM,"CUSTOM");
    public static int type_HSV = 0;
    public static int type_HISTOGRAM = 0;
    private int hmin;
    private int smin;
    private int vmin;
    private int hmax;
    private int smax;
    private int vmax;
    private Mat data;
    private int type;
    private String name;


    /**
     * Constructor from scalar objects
     * @param hmin H Channel min value
     * @param smin S Channel min value
     * @param vmin V Channel min value
     * @param hmax H Channel max value
     * @param smax S Channel max value
     * @param vmax V Channel max value
     * @param name Color name
     */
    public Blobcolor(int hmin, int smin, int vmin, int hmax, int smax, int vmax, String name){

        this.hmax = hmax;
        this.smax = smax;
        this.vmax = vmax;
        this.hmin = hmin;
        this.smin = smin;
        this.vmin = vmin;
        this.name = name;
        this.type = type_HSV;
    }

    public Blobcolor(Mat histogram, String name){
        this.data = histogram;
        this.name = name;
        this.type = type_HISTOGRAM;
    }

    /**
     * Gets the low hsv range from a color
     * @param color the color
     * @return a Scalar object with the range
     */
    public Scalar getLowRange(Blobcolor color){
        if (this.type == type_HSV) {
            return new Scalar(color.hmin, color.smin, color.vmin);
        }else {
            return null;
        }
    }

    /**
     * Gets the high hsv range from a color
     * @param color the color
     * @return a Scalar object with the range
     */
    public Scalar getHighRange(Blobcolor color){
        if (this.type == type_HSV) {
            return new Scalar(color.hmax, color.smax, color.vmax);
        }else {
            return null;
        }
    }

    /**
     * Creates a blobcolor from the calibrarion data
     * @param icolor The color
     * @param key Key for the retrieval of the calibration data
     * @return
     */
    public static Blobcolor blobColorFromColorCalibrationData(IColorCalibrationData icolor, String key) {

        if (icolor.getType() == IColorCalibrationData.type_HSV) {
            ColorCalibrationDataHSV color = (ColorCalibrationDataHSV) icolor;
            return new Blobcolor(
                    color.getMinH(),
                    color.getMinS(),
                    color.getMinV(),
                    color.getMaxH(),
                    color.getMaxS(),
                    color.getMaxV(),
                    key);
        }
        else {
            ColorCalibrationDataHistogram color = (ColorCalibrationDataHistogram) icolor;

            return new Blobcolor(color.getHistMat(),key);
        }
    }

    public Mat getHistogramData(){
        if(this.type == type_HISTOGRAM) {
            return this.data;
        }
        else{
            return null;
        }
    }

    public int getType(){
        return this.type;
    }
    /**
     * Returns the printable name of the color
     * @return The name
     */
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == this.getClass()){
            Blobcolor b = (Blobcolor) obj;
            if (b.name.equals(this.name)){
                return true;
            }
        }
            return false;

    }
}
