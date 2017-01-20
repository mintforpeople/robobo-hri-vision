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


import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
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

public class GoogleVisionCameraModule extends ACameraModule {
    private static final String TAG = "GoogleVisionCM";

    private FrameDetector frameDetector = new FrameDetector(this);

    private CameraSource mCameraSource = null;

    @Override
    public void signalInit() {


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
        Context context = manager.getApplicationContext();



        mCameraSource = new CameraSource.Builder(context, frameDetector)
                .setRequestedPreviewSize(240, 320)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .setAutoFocusEnabled(true)
                .build();

        try {
            mCameraSource.start();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        com.mytechia.robobo.framework.hri.vision.basicCamera.Frame f = new com.mytechia.robobo.framework.hri.vision.basicCamera.Frame();
        f.setBitmap(frame.getBitmap());
        f.setHeight(frame.getMetadata().getHeight());
        f.setWidth(frame.getMetadata().getWidth());
        f.setFrameId(frame.getMetadata().getId()+"");
        notifyFrame(f);

    }


}
