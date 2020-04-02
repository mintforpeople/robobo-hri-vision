package com.mytechia.robobo.framework.hri.vision.laneDetection;

import org.opencv.core.Mat;

public interface ILaneDetectionListener {
    void onLane(double a1, double b1,double  a2,double  b2);

    void onLane(Line line_lt, Line line_rt, Mat minv);
}
