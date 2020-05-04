package com.mytechia.robobo.framework.hri.vision.tag;

import android.graphics.PointF;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sqrt;
import static org.opencv.core.CvType.CV_64F;

public class Tag {
    private Mat corners;
    private int id;
    private ArrayList<PointF> cornerPoints;
    private double[] rvecs;
    private double[] tvecs;

    public Tag(Mat corners, double id, boolean flipped, int imageWidth) {
        this.corners = corners;
        this.id = (int) id;
        if (!flipped) {
            cornerPoints = new ArrayList<>(4);
            for (int i = 0; i < 4; i++) {


                PointF point = new PointF((float) corners.get(0, i)[0], (float) corners.get(0, i)[1]);
                cornerPoints.add(point);
            }
        } else {
            cornerPoints = new ArrayList<>(4);
            for (int i = 0; i < 4; i++) {
                PointF point;
                if ((float) corners.get(0, i)[0] < imageWidth / 2) {
                    point = new PointF((float) corners.get(0, i)[0] - (float) imageWidth / 2, (float) corners.get(0, i)[1]);

                } else {
                    point = new PointF(imageWidth - (float) corners.get(0, i)[0], (float) corners.get(0, i)[1]);

                }
                cornerPoints.set(i, point);
            }


        }
    }

    public Tag(Mat corners, double id, boolean flipped, int imageWidth, double[] rvecs, double[] tvecs) {
        this.corners = corners;
        this.id = (int) id;
        this.rvecs = rvecs;
        this.tvecs = tvecs;
        if (!flipped) {
            cornerPoints = new ArrayList<>(4);
            for (int i = 0; i < 4; i++) {


                PointF point = new PointF((float) corners.get(0, i)[0], (float) corners.get(0, i)[1]);
                cornerPoints.add(point);
            }
        } else {
            cornerPoints = new ArrayList<>(4);
            for (int i = 0; i < 4; i++) {
                PointF point;
                //if ((float)corners.get(0,i)[0] < imageWidth/2){
                point = new PointF((imageWidth) - (float) corners.get(0, i)[0], (float) corners.get(0, i)[1]);
                //point = new PointF((float)corners.get(0,i)[0],(float)corners.get(0,i)[1]);

                /*}else{
                    point = new PointF(imageWidth/2 - (float)corners.get(0,i)[0],(float)corners.get(0,i)[1]);

                }*/
                cornerPoints.add(point);
            }


        }
    }

    public Tag(Mat corners, double id) {
        this.corners = corners;
        this.id = (int) id;
        cornerPoints = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {


            PointF point = new PointF((float) corners.get(0, i)[0], (float) corners.get(0, i)[0]);
            cornerPoints.add(point);
        }

    }

    public Mat getCorners() {
        return corners;
    }

    public PointF getCorner(int cornerNumber) {
        return cornerPoints.get(cornerNumber);
    }

    public int getId() {
        return id;
    }

    public double[] getRvecs() {
        return rvecs;
    }

    public double[] getTvecs() {
        return tvecs;
    }

    public double[] getQuaternion() {
        double[] q = new double[4];
        Mat rvec = new Mat(1, 3, CvType.CV_64F),
                r = new Mat(3, 3, CvType.CV_64F);
        rvec.put(0, 0, rvecs);
        Calib3d.Rodrigues(rvec, r);


        double trace = r.get(0, 0)[0] +
                r.get(1, 1)[0] +
                r.get(2, 2)[0];
//
        if (trace > 0.0) {
            double s = sqrt(trace + 1.0);
            q[3] = (s * 0.5);
            s = 0.5 / s;
            q[0] = ((r.get(2, 1)[0] - r.get(1, 2)[0]) * s);
            q[1] = ((r.get(0, 2)[0] - r.get(2, 0)[0]) * s);
            q[2] = ((r.get(1, 0)[0] - r.get(0, 1)[0]) * s);
        } else {
            int i = r.get(0,0)[0] < r.get(1,1)[0] ?
            (r.get(1,1)[0] < r.get(2,2)[0] ? 2 : 1) :
            (r.get(0,0)[0] < r.get(2,2)[0] ? 2 : 0);
            int j = (i + 1) % 3;
            int k = (i + 2) % 3;
//
            double s = sqrt(r.get(i, i)[0] - r.get(j,j)[0] - r.get(k,k)[0] + 1.0);
            q[i] = s * 0.5;
            s = 0.5 / s;
//
            q[3] = (r.get(k,j)[0] - r.get(j,k)[0]) * s;
            q[j] = (r.get(j,i)[0] + r.get(i,j)[0]) * s;
            q[k] = (r.get(k,i)[0] + r.get(i,k)[0]) * s;
        }
        return q;
    }

    @Override
    public String toString() {
        String s = "";
        s = s + "ID: " + id + " Corners: ";
        for (int i = 0; i < 4; i++) {
            s = s + "(" + corners.get(0, i)[0] + ", " + corners.get(0, i)[1] + ")";
        }
        return s;
    }

    public static Mat drawArucos(List<Tag> tags, Mat image) {

        for (Tag tag : tags) {
            for (int i = 0; i < 4; i++) {
                Imgproc.line(image, new Point(tag.getCorner(i).x, tag.getCorner(i).y), new Point(tag.getCorner((i + 1) % 4).x, tag.getCorner((i + 1) % 4).y), new Scalar(255, 0, 0));

                Imgproc.circle(image, new Point(tag.getCorner(i).x, tag.getCorner(i).y), 3, new Scalar(0, 255, 0));
            }
        }

        return image;
    }
}
