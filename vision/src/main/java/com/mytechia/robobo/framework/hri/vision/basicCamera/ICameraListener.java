package com.mytechia.robobo.framework.hri.vision.basicCamera;


import org.opencv.core.Mat;

/**
 * Created by luis on 19/7/16.
 */
public interface ICameraListener {
    void onNewFrame(Frame frame);
    void onNewMat(Mat mat);
}
