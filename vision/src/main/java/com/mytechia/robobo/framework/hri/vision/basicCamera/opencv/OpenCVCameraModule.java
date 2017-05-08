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
package com.mytechia.robobo.framework.hri.vision.basicCamera.opencv;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Looper;
import android.renderscript.Script;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.IModule;
import com.mytechia.robobo.framework.LogLvl;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ACameraModule;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;


import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_BACK;
import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

/**
 * Implementation of the basic camera module using the OpenCV library
 */
public class OpenCVCameraModule extends ACameraModule implements CameraBridgeViewBase.CvCameraViewListener2 {
    //region VAR
    private static final String TAG = "OpenCVCameraModule";

    private CameraBridgeViewBase mOpenCvCameraView;
    private Context context;
    private boolean notifyBitmap = true;
    private boolean notifyMat = true;
    private int index = CAMERA_ID_FRONT;


    private int resolution_height = 640;
    private int resolution_width = 480;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    m.log(LogLvl.INFO, TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    //endregion


    //region IModule methods
    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        m =  manager;
        context = manager.getApplicationContext();
        Looper.prepare();

        Properties properties = new Properties();
        AssetManager assetManager = manager.getApplicationContext().getAssets();

        try {
            InputStream inputStream = assetManager.open("vision.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        resolution_height = Integer.parseInt(properties.getProperty("resolution_height"));
        resolution_width = Integer.parseInt(properties.getProperty("resolution_width"));



    }



    @Override
    public void shutdown() throws InternalErrorException {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public String getModuleInfo() {
        return "OpenCvCameraModule";
    }

    @Override
    public String getModuleVersion() {
        return "v0.1";
    }






    //endregion




    //region ICamera methods
    @Override
    public void signalInit() {

        //mOpenCvCameraView = (CameraBridgeViewBase) new JavaCameraView(context,1);

        //mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.disableFpsMeter();




        if (!OpenCVLoader.initDebug()) {
            m.log(LogLvl.WARNING, TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, context, mLoaderCallback);
        } else {
            m.log(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    @Override
    public void passSurfaceView(SurfaceView view) {

    }

    @Override
    public void passOCVthings(CameraBridgeViewBase bridgebase) {
        mOpenCvCameraView = bridgebase;
        ViewGroup.LayoutParams params=bridgebase.getLayoutParams();
        params.height = resolution_height;
        params.width = resolution_width;
        bridgebase.setLayoutParams(params);
    }

    @Override
    public void changeCamera() {


        switch (index){
            case CAMERA_ID_BACK:
                index = CAMERA_ID_FRONT;
                break;
            case CAMERA_ID_FRONT:
                index = CAMERA_ID_BACK;
                break;
        }
        m.log(TAG,"New camera index: "+index);
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(index);
        mOpenCvCameraView.enableView();
    }
    //endregion

    //region ICameraListener methods
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Bitmap bmp;
        Mat mat = inputFrame.rgba();


        //Giramos la imagen para evitar que salga torcida
        Core.flip(mat.t(),mat,1);
        bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);

        //Conversión de la matriz a bitmap
        Utils.matToBitmap(mat, bmp);
        Frame frame = new Frame();
        frame.setHeight(bmp.getHeight());
        frame.setWidth(bmp.getWidth());
        frame.setBitmap(bmp);
        //TODO devolver byte[] para artoolkit???
        notifyFrame(frame);

        if (notifyMat){
            notifyMat(mat);
        }

        return null;
    }
    //endregion

    //region OpenCV Methods
    @Override
    public void onCameraViewStarted(int width, int height) {

        m.log(TAG,"Camera view started, resolution: "+height+"x"+width);


    }

    @Override
    public void onCameraViewStopped() {
        m.log(TAG,"Camera view stopped");
    }

    //endregion
}
