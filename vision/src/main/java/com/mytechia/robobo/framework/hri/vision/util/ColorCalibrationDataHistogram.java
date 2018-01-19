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

import org.opencv.core.Mat;

/**
 * HSV Range of a color
 *
 * @author Luis Llamas luis.llamas@mytechia.com.
 */
public class ColorCalibrationDataHistogram extends AColorCalibrationData {
    private String data;

    public ColorCalibrationDataHistogram(String histogram){
        this.type = type_HIST;
        this.data = histogram;

    }

    public ColorCalibrationDataHistogram(Mat histogram){
        this.type = type_HIST;
        this.data = GsonConverter.matToJson(histogram);
    }

    public String getHist() {
        return this.data;
    }

    public Mat getHistMat(){
        return GsonConverter.matFromJson(this.data);
    }



    @Override
    public int getType() {
        return type_HIST;
    }
}
