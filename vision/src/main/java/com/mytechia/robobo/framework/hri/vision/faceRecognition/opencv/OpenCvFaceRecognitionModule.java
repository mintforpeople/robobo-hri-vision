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

package com.mytechia.robobo.framework.hri.vision.faceRecognition.opencv;

import android.graphics.Bitmap;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.basicCamera.*;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.faceRecognition.AFaceRecognitionModule;


import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_face.*;
import org.bytedeco.javacpp.opencv_imgcodecs.*;

import static org.bytedeco.javacpp.opencv_core.CV_8UC3;

/**
 * Implementation of the face recognition module using OpenCV
 */
public class OpenCvFaceRecognitionModule extends AFaceRecognitionModule {
//region VAR
   private FaceRecognizer faceRecognizer = new BasicFaceRecognizer(null);

//endregion
//region IModule Methods

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {

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

//endregion

//region IFaceRecognitionModule methods

    @Override
    public void train(Frame frame, int label) {
        Bitmap bmp = frame.getBitmap();
        Mat mat = new Mat(frame.getHeight(),frame.getWidth(),CV_8UC3 );

        bmp.copyPixelsToBuffer(mat.asByteBuffer());
        MatVector mv = new MatVector();
        mv.put(0,mat);

    }

    @Override
    public void identify(Frame frame) {

    }

//endregion
}
