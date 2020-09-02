package com.mytechia.robobo.framework.hri.vision.laneDetection;

import org.opencv.core.Mat;

public interface ILaneDetectionListener {
    void onLane(double slope_left, double bias_left, double slope_right, double bias_right);

    void onLane(Line line_lt, Line line_rt, Mat minv);
}
