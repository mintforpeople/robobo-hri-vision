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
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.faceDetection.AFaceDetectionModule;

import org.opencv.core.Mat;

/**
 * Created by luis on 24/7/16.
 * https://developers.google.com/android/reference/com/google/android/gms/vision/face/FaceDetector
 * todo mirar el setfocus
 */
public class AndroidFaceDetectionModule extends AFaceDetectionModule implements ICameraListener{


    //region VAR
    private String TAG = "FaceDetectionModule";
    private FaceDetector faceDetector;
    private FaceDetector.Face[] faces;
    float myEyesDistance;
    int numberOfFaceDetected;
    private boolean processing = false;
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
        if (!processing) {
            processing = true;

            Bitmap convertedBitmap = convert(frame.getBitmap(), Bitmap.Config.RGB_565);
            faceDetector = new FaceDetector(convertedBitmap.getWidth(), convertedBitmap.getHeight(), 1);
            //Log.d(TAG, "New Frame, resolution:"+convertedBitmap.getHeight()+"x"+convertedBitmap.getWidth());
            int facenumber = faceDetector.findFaces(convertedBitmap, faces);
            if (facenumber > 0) {
                PointF facecoord = new PointF();
                float eyesDistance = 0;
                faces[0].getMidPoint(facecoord);
                eyesDistance = faces[0].eyesDistance();
                notifyFace(facecoord, eyesDistance);

            }
            processing = false;
        }
    }

    @Override
    public void onNewMat(Mat mat) {

    }
    //endregion


}
