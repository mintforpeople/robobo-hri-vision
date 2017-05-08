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
package com.mytechia.robobo.framework.hri.vision.colorMesaurement.opencv;

import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.colorMesaurement.AColorMesaurementModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;


import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Implementation of the color measurement module using opencv
 */
public class OpenCVColorMesaurementModule extends AColorMesaurementModule implements ICameraListener {
    private ICameraModule cameraModule;
    private String TAG = "OCVColorMesaurement";
    private int lastr = 0;
    private int lastg = 0;
    private int lastb = 0;

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        m = manager;
        cameraModule = manager.getModuleInstance(ICameraModule.class);
        rcmodule = manager.getModuleInstance(IRemoteControlModule.class);
        cameraModule.suscribe(this);

    }

    @Override
    public void shutdown() throws InternalErrorException {
        cameraModule.unsuscribe(this);
    }

    @Override
    public String getModuleInfo() {
        return "Color Measurement Module";
    }

    @Override
    public String getModuleVersion() {
        return "0.3.0";
    }

    @Override
    public void onNewFrame(Frame frame) {

    }

    @Override
    public void onNewMat(Mat mat) {

//        Scalar results = Core.mean(mat);
//
//        Log.d(TAG,results.val[0]+"R");
//        Log.d(TAG,results.val[1]+"G");
//        Log.d(TAG,results.val[2]+"B");
//        mat.release();

        int r = 0;
        int g = 0;
        int b = 0;
        long pixels = mat.rows()/4* mat.cols()/4;
        long count =0;

        Mat hsvMat = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGB2HSV, 3);




        for (int row = 0; row<(hsvMat.rows()); row+=4){
            for (int col = 0; col<(hsvMat.cols()); col+=4) {
                //Log.d(TAG, "Row: "+row+" Col: "+col);
                double[] pixel = hsvMat.get(row,col);




                int hue = (int) Math.round(pixel[0]);
                int saturation = (int) Math.round(pixel[1]);
                int value = (int) Math.round(pixel[2]);

                if ((value > 120) && (saturation > 160)){
//                    Log.d(TAG,"Saturation:"+saturation);
//                    Log.d(TAG,"Value:"+value);
//                    Log.d(TAG,"Hue:"+hue);


                    hue = hue - 8;
                    count = count +1;
                    if (hue < 0) {
                        hue = 171 + Math.abs(hue);
                    }


                    if ((hue > 166) && (hue <= 179)) {
    //                    Log.d(TAG, "RED" + hue);
                        r = r + 1;
                    }

                    if ((hue > 0) && (hue <= 29)) {
    //                    Log.d(TAG, "YELLOW" + hue);
                        count = count -1;
                    }
                    if ((hue > 30) && (hue <= 89)) {
    //                    Log.d(TAG, "GREEN" + hue);
                        g = g + 1;
                    }
//                    if ((hue > 67) && (hue <= 96)) {
//    //                    Log.d(TAG, "CYAN" + hue);
//
//                        g = g + 1;
//
//
//                    }
                    if ((hue > 90) && (hue <= 141)) {
    //                    Log.d(TAG, "BLUE" + hue);
                        b = b + 1;

                    }
                    if ((hue > 142) && (hue <= 165)) {
    //                    Log.d(TAG, "MAGENTA" + hue);

                        r = r + 1;
                    }
                }

            }
        }

        int sum = r+g+b;
        //sum = (hsvMat.cols()*hsvMat.rows())/8;
//        Log.d(TAG,"Count: "+count);
        if((sum!=0)&&(count>3)) {
            r = Math.round(((float)r / (float)sum) * 100);
            g = Math.round(((float)g / (float)sum) * 100);
            b = Math.round(((float)b / (float)sum) * 100);

//            Log.d(TAG, "R: " + r + " G: " + g + " B: " + b+" Covered: "+(float)count/(float)pixels);
            if ((lastb!=b)||(lastg!=g)||(lastr!=r)) {
                notifyColorMesaured(r, g, b);
                lastb = b;
                lastg = g;
                lastr = r;
            }
        }
        else {
//            Log.d(TAG, "R: " + 0 + " G: " + 0 + " B: " + 0);
            if ((lastb!=b)||(lastg!=g)||(lastr!=r)) {
                notifyColorMesaured(0, 0, 0);
                lastb = 0;
                lastg = 0;
                lastr = 0;
            }

        }
        mat.release();
        hsvMat.release();


    }





}

