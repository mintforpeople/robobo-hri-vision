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
 * Created by luis on 24/10/16.
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
