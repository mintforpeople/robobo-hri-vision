package com.mytechia.robobo.framework.hri.vision.faceDetection.android;

import android.graphics.PointF;
import android.media.FaceDetector;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.faceDetection.AFaceDetectionModule;

/**
 * Created by luis on 24/7/16.
 * https://developers.google.com/android/reference/com/google/android/gms/vision/face/FaceDetector
 * todo mirar el setfocus
 */
public class AndroidFaceDetectionModule extends AFaceDetectionModule implements ICameraListener{


    //region VAR
    private FaceDetector faceDetector;
    private FaceDetector.Face[] faces;
    float myEyesDistance;
    int numberOfFaceDetected;
    private ICameraModule cameraModule;
    //endregion

    //region IModule methods

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        this.cameraModule = manager.getModuleInstance(ICameraModule.class);
        cameraModule.suscribe(this);
        faces =  new FaceDetector.Face[5];

    }

    @Override
    public void shutdown() throws InternalErrorException {
    }

    @Override
    public String getModuleInfo() {
        return "Android Face Detection Module";
    }

    @Override
    public String getModuleVersion() {
        return "v0.1";
    }



    //endregion

    //region ICameraListener Methods
    @Override
    public void onNewFrame(Frame frame) {
        faceDetector = new FaceDetector(frame.getBitmap().getWidth(),frame.getBitmap().getHeight(),1);
        int facenumber = faceDetector.findFaces(frame.getBitmap(),faces);
        if (facenumber>0){
            PointF facecoord = new PointF();
            float eyesDistance = 0;
            faces[0].getMidPoint(facecoord);
            eyesDistance = faces[0].eyesDistance();
            notifyFace(facecoord,eyesDistance);
        }
    }
    //endregion


}
