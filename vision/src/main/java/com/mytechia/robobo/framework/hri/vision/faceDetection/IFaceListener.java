package com.mytechia.robobo.framework.hri.vision.faceDetection;

import android.graphics.PointF;

/**
 * Created by luis on 24/7/16.
 */
public interface IFaceListener {
    public void onFaceDetected(PointF faceCoords, float eyesDistance);
}
