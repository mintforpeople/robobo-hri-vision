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

    public CameraCalibrationData() {
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
