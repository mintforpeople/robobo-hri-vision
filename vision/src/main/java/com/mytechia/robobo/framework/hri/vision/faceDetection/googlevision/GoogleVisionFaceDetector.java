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
package com.mytechia.robobo.framework.hri.vision.faceDetection.googlevision;



import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;

import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.faceDetection.AFaceDetectionModule;

import org.opencv.core.Mat;


public class GoogleVisionFaceDetector extends AFaceDetectionModule implements ICameraListener{
    private FaceDetector detector;
    private ICameraModule cameraModule;
    private boolean processing = false;
    private String TAG = "GoogleFaceModule";
    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        cameraModule = manager.getModuleInstance(ICameraModule.class);
        cameraModule.suscribe(this);
        detector = new FaceDetector.Builder(manager.getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setProminentFaceOnly(true)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE)
                .build();
    }

    @Override
    public void shutdown() throws InternalErrorException {
        if (detector!=null){
            detector.release();
        }
    }

    @Override
    public String getModuleInfo() {
        return "GoogleFaceDetector";
    }

    @Override
    public String getModuleVersion() {
        return "0.1";
    }

    @Override
    public void onNewFrame(com.mytechia.robobo.framework.hri.vision.basicCamera.Frame frame) {
        if (!processing) {
            processing = true;
            Frame f = new Frame.Builder().setBitmap(frame.getBitmap()).build();
            SparseArray<Face> faces = detector.detect(f);
            if (faces.size() > 0) {
                Face face = faces.get(0);
                Log.d(TAG,face.toString());
                notifyFace(face.getPosition(), face.getWidth());
            }
            processing = false;
        }
    }

    @Override
    public void onNewMat(Mat mat) {

    }
}
