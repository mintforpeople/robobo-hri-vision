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
package com.mytechia.robobo.framework.hri.vision.basicCamera.googlevision;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;
import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ACameraModule;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;

import org.opencv.android.CameraBridgeViewBase;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class GoogleVisionCameraModule extends ACameraModule {
    private static final String TAG = "GoogleVisionCM";

    private FrameDetector frameDetector = new FrameDetector(this);

    private CameraSource mCameraSource = null;

    private Context context;

    @Override
    public void signalInit() {

        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCameraSource.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                mCameraSource.takePicture(new CameraSource.ShutterCallback() {
//                    @Override
//                    public void onShutter() {
//
//                    }
//                }, new CameraSource.PictureCallback() {
//                    @Override
//                    public void onPictureTaken(byte[] bytes) {
//
//                    }
//                });
//            }
//
//
//        },330,1000);
    }

    @Override
    public void signalInit(SurfaceHolder sh) {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mCameraSource.start(sh);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void passSurfaceView(SurfaceView view) {

    }

    @Override
    public void passOCVthings(CameraBridgeViewBase bridgebase) {

    }

    @Override
    public void changeCamera() {

    }

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
         context = manager.getApplicationContext();



        mCameraSource = new CameraSource.Builder(context, frameDetector)
                .setRequestedPreviewSize(240, 320)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .setAutoFocusEnabled(true)
                .build();

//        try {
//            mCameraSource.start();
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void shutdown() throws InternalErrorException {

    }

    @Override
    public String getModuleInfo() {
        return null;
    }

    @Override
    public String getModuleVersion() {
        return null;
    }

    protected void receiveFrame(Frame frame){


//        byte[] imageBytes= new byte[frame.getGrayscaleImageData().remaining()];
//        frame.getGrayscaleImageData().get(imageBytes);
//        final Bitmap bmp=BitmapFactory.decodeStream();
//        Bitmap bitmap = Bitmap.createBitmap(frame.getMetadata().getWidth(), frame.getMetadata().getHeight(), Bitmap.Config.);
//        bitmap.copyPixelsFromBuffer(frame.getGrayscaleImageData());


        com.mytechia.robobo.framework.hri.vision.basicCamera.Frame f = new com.mytechia.robobo.framework.hri.vision.basicCamera.Frame();
        f.setBitmap(frame.getBitmap());
        f.setHeight(frame.getMetadata().getHeight());
        f.setWidth(frame.getMetadata().getWidth());
        f.setFrameId(frame.getMetadata().getId()+"");
        notifyFrame(f);

    }


}
