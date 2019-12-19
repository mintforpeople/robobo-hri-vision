package com.mytechia.robobo.framework.hri.vision.aruco;

import org.opencv.core.Mat;

import java.util.List;

public interface IArucoListener {
    //void onAruco(List<ArucoTag> marker);
    void onAruco(List<Mat> corners, Mat ids);
}
