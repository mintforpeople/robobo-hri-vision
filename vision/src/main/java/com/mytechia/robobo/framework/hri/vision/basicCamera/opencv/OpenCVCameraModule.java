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
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.LogLvl;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ACameraModule;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.util.FrameCounter;
import com.mytechia.robobo.framework.power.IPowerModeListener;
import com.mytechia.robobo.framework.power.PowerMode;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;


import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_BACK;
import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

/**
 * Implementation of the basic camera module using the OpenCV library
 */
public class OpenCVCameraModule extends ACameraModule implements CameraBridgeViewBase.CvCameraViewListener2, IPowerModeListener {
    //region VAR
    private static final String TAG = "OpenCVCameraModule";

    // OpenCV Camera Bridge
    private CameraBridgeViewBase mOpenCvCameraView;
    private Context context;

    // Flags for enabling different image types
    private boolean notifyBitmap = true;
    private boolean notifyMat = true;

    // Index of the camera being used
    private int index = CAMERA_ID_FRONT;

    // Flag for showing the image on the opencv view, used for debug purposes
    private boolean showImgInView = false;

    // Camera resolution
    private int resolution_height = 640;
    private int resolution_width = 480;

    // FPS control variables
    private long lastFrameTime = 0;
    private long deltaTimeThreshold = 17;

    private FrameCounter fps = new FrameCounter();

    // Remote control module instance
    private IRemoteControlModule remoteControlModule;
    private int seqnum = 0;



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    roboboManager.log(LogLvl.INFO, TAG, "OpenCV loaded successfully");
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


    @Override
    public void onPowerModeChange(PowerMode newMode) {

        // On low power mode disable the view to stop the camera capture
        if (newMode == PowerMode.LOWPOWER) {
            mOpenCvCameraView.disableView();
        }
        else {
            mOpenCvCameraView.enableView();
        }

    }

    //region IModule methods
    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        roboboManager =  manager;

        // Get instance of the remote module
        try {
            remoteControlModule = manager.getModuleInstance(IRemoteControlModule.class);
        }catch (ModuleNotFoundException e){
            e.printStackTrace();
        }

        context = manager.getApplicationContext();

        // Load properties form resources file
        Properties properties = new Properties();
        AssetManager assetManager = manager.getApplicationContext().getAssets();

        try {
            InputStream inputStream = assetManager.open("vision.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            resolution_height = Integer.parseInt(properties.getProperty("resolution_height"));
            resolution_width = Integer.parseInt(properties.getProperty("resolution_width"));
        }
        catch (NumberFormatException e){
            roboboManager.log(LogLvl.WARNING,TAG,"Properties not defined, using defaults");
        }

        // Register the command to change de camera in use
        remoteControlModule.registerCommand("SET-CAMERA", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                if(c.getParameters().containsKey("camera")){
                    changeCamera(c.getParameters().get("camera"));
                }
            }
        });

        remoteControlModule.registerCommand("START-CAMERA", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                mOpenCvCameraView.enableView();
            }
        });

        remoteControlModule.registerCommand("STOP-CAMERA", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                mOpenCvCameraView.disableView();
            }
        });
        manager.subscribeToPowerModeChanges(this);

    }



    @Override
    public void shutdown() throws InternalErrorException {
        if (mOpenCvCameraView != null) {
            Log.i(TAG, "OpenCVCameraModule shutdown");
            mOpenCvCameraView.disableView();
        }
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
            roboboManager.log(LogLvl.WARNING, TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, context, mLoaderCallback);
        } else {
            roboboManager.log(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        notifyOpenCVStartup();
    }


    @Override
    public void passSurfaceView(SurfaceView view) {

    }

    @Override
    public void passOCVthings(CameraBridgeViewBase bridgebase) {
        mOpenCvCameraView = bridgebase;

        // Setting the view to the desired resolution
        ViewGroup.LayoutParams params = bridgebase.getLayoutParams();
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
        roboboManager.log(TAG,"New camera index: "+index);
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(index);
        mOpenCvCameraView.enableView();
    }

    /**
     * Method for changing to a especific camera
     * @param camera
     */
    public  void changeCamera(String camera){
        if (camera.equals("back")){
            index = CAMERA_ID_BACK;

        }else {
            index = CAMERA_ID_FRONT;

        }
        roboboManager.log(TAG,"New camera index: "+index);
        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(index);
        mOpenCvCameraView.enableView();
    }

    @Override
    public void debugFrame(Frame frame, String frameId) {
        notifyDebugFrame(frame, frameId);
    }

    @Override
    public void showFrameInView(boolean set) {
        showImgInView = set;
    }

    @Override
    public void setFps(int fps) {
        deltaTimeThreshold = 1000/fps;
    }
    //endregion

    //region ICameraListener methods
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        long millis = System.currentTimeMillis();
        // Check if we want to process a new frame
        if (millis-lastFrameTime>=deltaTimeThreshold) {
           // roboboManager.log("CameraModule",millis-lastFrameTime+"");

            lastFrameTime = millis;
            Bitmap bmp;
            // Obtain the RGB version of the image in OCV Mat format
            Mat mat = inputFrame.rgba();


            //Giramos la imagen para evitar que salga torcida
            //Core.flip(mat.t(), mat, 1);

            // TODO eliminar esta linea
            //bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);

            //Conversión de la matriz a bitmap
//        Utils.matToBitmap(mat, bmp);
//        Frame frame = new Frame();
//        frame.setHeight(bmp.getHeight());
//        frame.setWidth(bmp.getWidth());
//        frame.setBitmap(bmp);
            this.seqnum = this.seqnum + 1;

            Frame frame = new Frame(mat);
            frame.setSeqNum(this.seqnum);
            notifyFrame(frame);

            if (notifyMat) {
                notifyMat(mat);
            }

            // Update frame counter
            fps.newFrame();

            if (fps.getElapsedTime() % 10 == 0) {
                roboboManager.log(LogLvl.TRACE, "CAMERA", "FPS = " + fps.getFPS());
            }

        }

        // Return null to not show any image in the view
        if (showImgInView){
            return inputFrame.rgba();
        }else {
            return null;
        }

    }
    //endregion

    //region OpenCV Methods
    @Override
    public void onCameraViewStarted(int width, int height) {
        resolution_height = height;
        resolution_width = width;
        roboboManager.log(TAG,"Camera view started, resolution: "+height+"x"+width);


    }

    @Override
    public void onCameraViewStopped() {
        roboboManager.log(TAG,"Camera view stopped");
    }

    //endregion

    @Override
    public int getResX() {
        return resolution_width;
    }

    @Override
    public int getResY() {
        return resolution_height;
    }

    @Override
    public int getCameraCode() {
        return index;
    }
}
