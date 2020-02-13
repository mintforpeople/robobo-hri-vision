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
package com.mytechia.robobo.framework.hri.vision.faceDetection.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.Log;
import android.util.StringBuilderPrinter;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.LogLvl;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.faceDetection.AFaceDetectionModule;
import com.mytechia.robobo.framework.hri.vision.util.FrameCounter;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.core.Mat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of the face detection module using the android face detection API
 */
public class AndroidFaceDetectionModule extends AFaceDetectionModule implements ICameraListener{

    /*
     * https://developers.google.com/android/reference/com/google/android/gms/vision/face/FaceDetector
     */
    //region VAR
    private String TAG = "FaceDetectionModule";
    private int LOST_THRESHOLD = 3;
    private FaceDetector faceDetector;
    private FaceDetector.Face[] faces;
    float myEyesDistance;
    int numberOfFaceDetected;
    private boolean processing = false;
    private ICameraModule cameraModule;
    private int noDetectionCount = 0;
    private boolean lostFace = true;
    private boolean active = false;
    private boolean firstFrame = true;

    // Used to avoid blocking the onNewFrame callback thread
    ExecutorService executor;

    //endregion

    private FrameCounter fps = new FrameCounter();

    //region IModule methods

    @Override
    public void startup(RoboboManager manager)  {
        m = manager;
        // Load camera module and remote module instances
        try {
            this.cameraModule = manager.getModuleInstance(ICameraModule.class);
            rcmodule = manager.getModuleInstance(IRemoteControlModule.class);
        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }
        faces =  new FaceDetector.Face[5];

        executor = Executors.newFixedThreadPool(1);
        rcmodule.registerCommand("START-FACE-DETECTION", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                startDetection();
            }
        });

        rcmodule.registerCommand("STOP-FACE-DETECTION", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                pauseDetection();

            }
        });

        //TODO: Evaluate if start method should be called here or on each activity
    }

    @Override
    public void shutdown() {
        cameraModule.unsuscribe(this);

    }

    @Override
    public String getModuleInfo() {
        return "Android Face Detection Module";
    }

    @Override
    public String getModuleVersion() {
        return "0.3.0";
    }



    //endregion

    /**
     * Converts a bitmap to another one with the desired configuration
     * @param bitmap Bitmap to be converted
     * @param config Configuration to be applied
     * @return Converted bitmap
     */
    private Bitmap convert(Bitmap bitmap, Bitmap.Config config) {
        Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
        Canvas canvas = new Canvas(convertedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return convertedBitmap;
    }

    //region ICameraListener Methods
    @Override
    public void onNewFrame(Frame frame) {

        // Get resolution and create the face detector object
        if (firstFrame){
            resolutionX = frame.getWidth();
            resolutionY = frame.getHeight();
            faceDetector = new FaceDetector((int)resolutionX,(int) resolutionY, 1);
            firstFrame = false;

        }

        // If the module is not paused
        if (active) {

            final Frame finalFrame = frame;
            // If it's not already processing a frame
            if (!processing) {
                // Execute inside a thread to avoid locking the onNewFrame callback
                executor.execute(new Runnable() {
                    @Override
                    public void run() {

                        processing = true;

                        Bitmap convertedBitmap = convert(finalFrame.getBitmap(), Bitmap.Config.RGB_565);
                        //TODO Crear el detector solo una vez
                        //Log.d(TAG, "New Frame, resolution:"+convertedBitmap.getHeight()+"x"+convertedBitmap.getWidth());
                        int facenumber = faceDetector.findFaces(convertedBitmap, faces);

                        // If there is faces, get the firto one and obtain infotrmation
                        if (facenumber > 0) {

                            PointF facecoord = new PointF();
                            float eyesDistance = 0;
                            faces[0].getMidPoint(facecoord);


                            eyesDistance = faces[0].eyesDistance();
                            if (lostFace) {
                                lostFace = false;
                                notifyFaceAppear(facecoord, eyesDistance);
                            }
                            notifyFace(facecoord, eyesDistance);
                            noDetectionCount = 0;

                        }
                        // If there is no faces update de not detection count
                        else {
                            noDetectionCount += 1;
                            // If the count is greater than the threshold, notify a lost face to the
                            // listeners
                            if ((noDetectionCount > LOST_THRESHOLD) && (!lostFace)) {
                                notifyFaceDisappear();
                                lostFace = true;
                            }

                        }
                        processing = false;

                        // Update the frame counter
                        fps.newFrame();

                        if (fps.getElapsedTime() % 10 == 0) {
                            m.log(LogLvl.TRACE, "FACE", "FPS = " + fps.getFPS());
                        }


                    }
                });


            }
        }
    }

    @Override
    public void onNewMat(Mat mat) {

    }
    //endregion

    @Override
    public void onDebugFrame(Frame frame, String frameId) {

    }

    @Override
    public void onOpenCVStartup() {

    }

    @Override
    public void startDetection() {
        active = true;
        cameraModule.suscribe(this);

    }

    @Override
    public void pauseDetection() {
        active = false;
        cameraModule.unsuscribe(this);

    }

    @Override
    public void setLostThreshold(int threshold) {
        LOST_THRESHOLD = threshold;
    }
}
