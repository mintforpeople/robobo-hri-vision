package com.mytechia.robobo.framework.hri.vision.laneDetection;

import org.opencv.core.Mat;

public interface ILaneDetectionListener {
    void onLane(Mat markers);

    void onLane(Line line_lt, Line line_rt, Mat minv);
}
