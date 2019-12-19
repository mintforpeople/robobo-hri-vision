package com.mytechia.robobo.framework.hri.vision.aruco;

import android.graphics.PointF;

import org.opencv.core.Mat;

import java.util.ArrayList;

public class ArucoTag {
    private Mat corners;
    private double id;
    private ArrayList<PointF> cornerPoints;

    public ArucoTag(Mat corners, double id, boolean flipped, int width){
        this.corners = corners;
        this.id = id;
        cornerPoints = new ArrayList<>(4);
        for (int i = 0; i < 4; i++){


            PointF point = new PointF((float)corners.get(0,i)[0],(float)corners.get(0,i)[0]);
            cornerPoints.set(i,point);
        }

    }

    public ArucoTag(Mat corners, double id){
        this.corners = corners;
        this.id = id;
        cornerPoints = new ArrayList<>(4);
        for (int i = 0; i < 4; i++){


            PointF point = new PointF((float)corners.get(0,i)[0],(float)corners.get(0,i)[0]);
            cornerPoints.set(i,point);
        }

    }

    public Mat getCorners() {
        return corners;
    }

    public PointF getCorner(int cornerNumber){
        return cornerPoints.get(cornerNumber);
    }

    public double getId() {
        return id;
    }



    @Override
    public String toString() {
        String s = "";
        s = s + "ID: " + id + " Corners: ";
        for (int i = 0; i < 4; i++){
            s = s + "(" +corners.get(0,i)[0]+ ", "+ corners.get(0,i)[1] +")";
        }
        return s;
    }
}
