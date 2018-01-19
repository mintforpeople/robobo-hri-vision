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
    private IColorCalibrationData red;
    private IColorCalibrationData green;
    private IColorCalibrationData blue;
    private IColorCalibrationData custom;


    //default calibration values
    public static IColorCalibrationData DEFAULT_RED = new ColorCalibrationDataHSV(82,102,184,164,190,246);
    public static IColorCalibrationData DEFAULT_GREEN = new ColorCalibrationDataHSV(46,100 ,127,68,206,221);
    public static IColorCalibrationData DEFAULT_BLUE = new ColorCalibrationDataHSV(13,133,121,19,255,209);
    public static IColorCalibrationData DEFAULT_CUSTOM = new ColorCalibrationDataHSV(47,96,110,137,255,218);


    public CameraCalibrationData() {
        this(DEFAULT_RED, DEFAULT_GREEN, DEFAULT_BLUE, DEFAULT_CUSTOM);
    }


    public CameraCalibrationData(IColorCalibrationData red, IColorCalibrationData green,
                                 IColorCalibrationData blue, IColorCalibrationData custom) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.custom = custom;
    }

    public IColorCalibrationData getRed() {
        return red;
    }

    public void setRed(IColorCalibrationData red) {
        this.red = red;
    }

    public IColorCalibrationData getGreen() {
        return green;
    }

    public void setGreen(IColorCalibrationData green) {
        this.green = green;
    }

    public IColorCalibrationData getBlue() {
        return blue;
    }

    public void setBlue(IColorCalibrationData blue) {
        this.blue = blue;
    }

    public IColorCalibrationData getCustom() {
        return custom;
    }

    public void setCustom(IColorCalibrationData custom) {
        this.custom = custom;
    }
}
