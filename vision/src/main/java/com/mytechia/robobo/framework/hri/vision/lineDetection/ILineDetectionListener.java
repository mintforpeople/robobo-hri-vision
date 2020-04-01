package com.mytechia.robobo.framework.hri.vision.lineDetection;

import org.opencv.core.Mat;

public interface ILineDetectionListener {
    void onLine(Mat markers);
    //void onAruco(List<Mat> corners, Mat ids);
}
