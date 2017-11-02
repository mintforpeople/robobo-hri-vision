/**
 * *****************************************************************************
 * <p>
 * Copyright (C) 2017 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 * Copyright (C) 2017 Luis Llamas <luis.llamas@mytechia.com>
 * <p>
 * This file is part of Robobo App Setup.
 * ****************************************************************************
 */
package com.mytechia.robobo.framework.hri.vision.util;

import java.io.Serializable;

/**
 * Color ranges for a specific device
 *
 * @author Luis Llamas luis.llamas@mytechia.com.
 */
public class CameraCalibrationData implements Serializable {
    private ColorCalibrationData red;
    private ColorCalibrationData green;
    private ColorCalibrationData blue;
    private ColorCalibrationData custom;


    //default calibration values
    public static ColorCalibrationData DEFAULT_RED = new ColorCalibrationData(82,102,184,164,190,246);
    public static ColorCalibrationData DEFAULT_GREEN = new ColorCalibrationData(46,100 ,127,68,206,221);
    public static ColorCalibrationData DEFAULT_BLUE = new ColorCalibrationData(13,133,121,19,255,209);
    public static ColorCalibrationData DEFAULT_CUSTOM = new ColorCalibrationData(47,96,110,137,255,218);


    public CameraCalibrationData() {
        this(DEFAULT_RED, DEFAULT_GREEN, DEFAULT_BLUE, DEFAULT_CUSTOM);
    }


    public CameraCalibrationData(ColorCalibrationData red, ColorCalibrationData green,
                                 ColorCalibrationData blue, ColorCalibrationData custom) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.custom = custom;
    }

    public ColorCalibrationData getRed() {
        return red;
    }

    public void setRed(ColorCalibrationData red) {
        this.red = red;
    }

    public ColorCalibrationData getGreen() {
        return green;
    }

    public void setGreen(ColorCalibrationData green) {
        this.green = green;
    }

    public ColorCalibrationData getBlue() {
        return blue;
    }

    public void setBlue(ColorCalibrationData blue) {
        this.blue = blue;
    }

    public ColorCalibrationData getCustom() {
        return custom;
    }

    public void setCustom(ColorCalibrationData custom) {
        this.custom = custom;
    }
}
