package com.mytechia.robobo.framework.hri.vision.tag;

import android.graphics.PointF;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Tag {
    private Mat corners;
    private double id;
    private ArrayList<PointF> cornerPoints;
    private float[] rvecs;
    private float[] tvecs;

    public Tag(Mat corners, double id, boolean flipped, int imageWidth){
        this.corners = corners;
        this.id = id;
        if (!flipped){
            cornerPoints = new ArrayList<>(4);
            for (int i = 0; i < 4; i++){


                PointF point = new PointF((float)corners.get(0,i)[0],(float)corners.get(0,i)[1]);
                cornerPoints.add(point);
            }
        }else {
            cornerPoints = new ArrayList<>(4);
            for (int i = 0; i < 4; i++){
                PointF point;
                if ((float)corners.get(0,i)[0] < imageWidth/2){
                    point = new PointF((float)corners.get(0,i)[0]-(float)imageWidth/2,(float)corners.get(0,i)[1]);

                }else{
                    point = new PointF(imageWidth - (float)corners.get(0,i)[0],(float)corners.get(0,i)[1]);

                }
                cornerPoints.set(i,point);
            }


        }
    }

    public Tag(Mat corners, double id, boolean flipped, int imageWidth, float[] rvecs, float[] tvecs){
        this.corners = corners;
        this.id = id;
        this.rvecs = rvecs;
        this.tvecs = tvecs;
        if (!flipped){
            cornerPoints = new ArrayList<>(4);
            for (int i = 0; i < 4; i++){


                PointF point = new PointF((float)corners.get(0,i)[0],(float)corners.get(0,i)[1]);
                cornerPoints.add(point);
            }
        }else {
            cornerPoints = new ArrayList<>(4);
            for (int i = 0; i < 4; i++){
                PointF point;
                //if ((float)corners.get(0,i)[0] < imageWidth/2){
                    point = new PointF((imageWidth)-(float)corners.get(0,i)[0],(float)corners.get(0,i)[1]);
                    //point = new PointF((float)corners.get(0,i)[0],(float)corners.get(0,i)[1]);

                /*}else{
                    point = new PointF(imageWidth/2 - (float)corners.get(0,i)[0],(float)corners.get(0,i)[1]);

                }*/
                cornerPoints.add(point);
            }


        }
    }

    public Tag(Mat corners, double id){
        this.corners = corners;
        this.id = id;
        cornerPoints = new ArrayList<>(4);
        for (int i = 0; i < 4; i++){


            PointF point = new PointF((float)corners.get(0,i)[0],(float)corners.get(0,i)[0]);
            cornerPoints.add(point);
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

    public float[] getRvecs() {
        return rvecs;
    }

    public float[] getTvecs() {
        return tvecs;
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

    public static Mat drawArucos(List<Tag> tags, Mat image){

        for (Tag tag : tags){
            for (int i = 0; i < 4; i++){
                Imgproc.line(image, new Point(tag.getCorner(i).x,tag.getCorner(i).y),new Point(tag.getCorner((i+1)%4).x,tag.getCorner((i+1)%4).y),new Scalar(255,0,0));

                Imgproc.circle(image,new Point(tag.getCorner(i).x,tag.getCorner(i).y),3, new Scalar(0,255,0));
            }
        }

        return image;
    }
}
