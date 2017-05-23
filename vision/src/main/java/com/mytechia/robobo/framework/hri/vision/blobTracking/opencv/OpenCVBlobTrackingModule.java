/*******************************************************************************
 *
 *   Copyright 2017 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2017 Luis Llamas <luis.llamas@mytechia.com>
 *
 *   This file is part of Robobo HRI Modules.
 *
 *   Robobo HRI Modules is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo HRI Modules is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo HRI Modules.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package com.mytechia.robobo.framework.hri.vision.blobTracking.opencv;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.LogLvl;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.blobTracking.ABlobTrackingModule;
import com.mytechia.robobo.framework.hri.vision.blobTracking.Blob;
import com.mytechia.robobo.framework.hri.vision.blobTracking.Blobcolor;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

//http://www.pyimagesearch.com/2015/09/14/ball-tracking-with-opencv/
//https://github.com/badlogic/opencv-fun/blob/master/src/pool/utils/BallDetector.java
public class OpenCVBlobTrackingModule extends ABlobTrackingModule implements ICameraListener {
    private ICameraModule cameraModule;
    private boolean processing =false;
    private boolean dR =true;
    private boolean dG = false;
    private boolean dB = false;
    @Override
    public void onNewFrame(Frame frame) {
        
    }

    @Override
    public void onNewMat(Mat mat) {
    if (!processing) {
        Mat hsvFrame = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, hsvFrame, Imgproc.COLOR_BGR2HSV);
        Imgproc.blur(hsvFrame, hsvFrame, new Size(11, 11));

        if (dR) {
            Mat maskred = new Mat(mat.rows(), mat.cols(), CvType.CV_32F);

            Core.inRange(hsvFrame, Blobcolor.getLowRange(Blobcolor.RED), Blobcolor.getHighRange(Blobcolor.RED), maskred);
//            cameraModule.debugFrame(new Frame(maskred),"RED");

            //Clean mask and find contours
            Imgproc.erode(maskred, maskred, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(11, 11)));
            Imgproc.dilate(maskred, maskred, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(11, 11)));
            List<MatOfPoint> contoursred = new ArrayList<MatOfPoint>();
            Imgproc.findContours(maskred, contoursred, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            if (contoursred.size() > 0) {
                double maxarea = 0;
                MatOfPoint maxcontour = null;
                for (MatOfPoint c : contoursred) {
                    if (Imgproc.contourArea(c) > maxarea) {
                        maxcontour = c;
                        maxarea = Imgproc.contourArea(c);
                    }
                }
                MatOfPoint2f maxcontourf = new MatOfPoint2f(maxcontour.toArray());
                float[] radius = new float[1];
                Point center = new Point();
                Imgproc.minEnclosingCircle(maxcontourf, center, radius);
                double circularity =  Imgproc.contourArea(maxcontour)/(Math.PI*radius[0]*radius[0]);
                if (radius[0] > 10) {
                    Blob b = null;
                    if ( circularity >  0.70){
                        b = new Blob(Blobcolor.RED, center, (int)radius[0],true,false);
                    }else {
                        RotatedRect rrect = Imgproc.minAreaRect(maxcontourf);
                        double quadrangularity = Imgproc.contourArea(maxcontour)/rrect.size.area();
                        if (quadrangularity > 0.75) {

                            b = new Blob(Blobcolor.RED, center, (int) radius[0], false, true);
                        }
                        else {
                            b = new Blob(Blobcolor.RED, center, (int)radius[0],false,false);
                        }

                    }

                    this.notifyTrackingBlob(b);
                }

            }
        }
        if (dG) {
            Mat maskgreen = new Mat(mat.rows(), mat.cols(), CvType.CV_32F);

            Core.inRange(hsvFrame, Blobcolor.getLowRange(Blobcolor.GREEN), Blobcolor.getHighRange(Blobcolor.GREEN), maskgreen);
//            cameraModule.debugFrame(new Frame(maskgreen),"GREEN");

            Imgproc.erode(maskgreen, maskgreen, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(11, 11)));
            Imgproc.dilate(maskgreen, maskgreen, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(11, 11)));

            List<MatOfPoint> contoursgreen = new ArrayList<MatOfPoint>();
            Imgproc.findContours(maskgreen, contoursgreen, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            if (contoursgreen.size() > 0) {
                double maxarea = 0;
                MatOfPoint maxcontour = null;
                for (MatOfPoint c : contoursgreen) {
                    if (Imgproc.contourArea(c) > maxarea) {
                        maxcontour = c;
                        maxarea = Imgproc.contourArea(c);
                    }
                }
                MatOfPoint2f maxcontourf = new MatOfPoint2f(maxcontour.toArray());
                float[] radius = new float[1];
                Point center = new Point();
                Imgproc.minEnclosingCircle(maxcontourf, center, radius);
                double circularity =  Imgproc.contourArea(maxcontour)/(Math.PI*radius[0]*radius[0]);
                if (radius[0] > 10) {
                    Blob b = null;
                    if ( circularity >  0.70){
                        b = new Blob(Blobcolor.GREEN, center, (int)radius[0],true,false);
                    }else {
                        RotatedRect rrect = Imgproc.minAreaRect(maxcontourf);
                        double quadrangularity = Imgproc.contourArea(maxcontour)/rrect.size.area();
                        if (quadrangularity > 0.75) {
                            b = new Blob(Blobcolor.GREEN, center, (int) radius[0], false, true);
                        }
                        else {
                            b = new Blob(Blobcolor.GREEN, center, (int)radius[0],false,false);
                        }

                    }

                    this.notifyTrackingBlob(b);
                }

            }
        }
        if (dB) {
            Mat maskblue = new Mat(mat.rows(), mat.cols(), CvType.CV_32F);

            Core.inRange(hsvFrame, Blobcolor.getLowRange(Blobcolor.BLUE), Blobcolor.getHighRange(Blobcolor.BLUE), maskblue);
//            cameraModule.debugFrame(new Frame(maskblue),"BLUE");

            //Clean mask and find contours
            Imgproc.erode(maskblue, maskblue, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(11, 11)));
            Imgproc.dilate(maskblue, maskblue, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(11, 11)));
            List<MatOfPoint> contoursblue = new ArrayList<MatOfPoint>();
            Imgproc.findContours(maskblue, contoursblue, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            if (contoursblue.size() > 0) {
                double maxarea = 0;
                MatOfPoint maxcontour = null;
                for (MatOfPoint c : contoursblue) {
                    if (Imgproc.contourArea(c) > maxarea) {
                        maxcontour = c;
                        maxarea = Imgproc.contourArea(c);
                    }
                }
                MatOfPoint2f maxcontourf = new MatOfPoint2f(maxcontour.toArray());
                float[] radius = new float[1];
                Point center = new Point();
                Imgproc.minEnclosingCircle(maxcontourf, center, radius);

                double circularity =  Imgproc.contourArea(maxcontour)/(Math.PI*radius[0]*radius[0]);
                if (radius[0] > 10) {
                    Blob b = null;
                    if ( circularity >  0.70){
                        b = new Blob(Blobcolor.BLUE, center, (int)radius[0],true,false);
                    }else {
                        RotatedRect rrect = Imgproc.minAreaRect(maxcontourf);
                        double quadrangularity = Imgproc.contourArea(maxcontour)/rrect.size.area();

                        if (quadrangularity > 0.75) {
                            b = new Blob(Blobcolor.BLUE, center, (int) radius[0], false, true);
                        }
                        else {
                            b = new Blob(Blobcolor.BLUE, center, (int)radius[0],false,false);
                        }

                    }

                    this.notifyTrackingBlob(b);


                }
            }
        }

        //Get mask image for each color

        //Clean mask and find contours





//        Imgproc.GaussianBlur(maskgreen, maskgreen, new Size(9, 9), 2, 2);
//        Core.MinMaxLocResult minmax = Core.minMaxLoc(maskgreen);
//        m.log(LogLvl.WARNING, "ColorBlob", "Min: " + minmax.minVal + " Max: " + minmax.maxVal);
//        Mat circles = new Mat();
//
//        Imgproc.HoughCircles(maskgreen, circles, Imgproc.HOUGH_GRADIENT, 1, 1, 10, 20, 0, 0);
//        if (circles.cols() > 0)
//            for (int x = 0; x < circles.cols(); x++) {
//                double vCircle[] = circles.get(0, x);
//
//                if (vCircle == null)
//                    break;
//
//                Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
//                int radius = (int) Math.round(vCircle[2]);
//                notifyTrackingBlob(Blobcolor.GREEN, (int) pt.x, (int) pt.y, radius);
//            }
    }



    }

    @Override
    public void onDebugFrame(Frame frame, String frameId) {

    }


    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        m = manager;
        cameraModule = m.getModuleInstance(ICameraModule.class);
        cameraModule.suscribe(this);
    }

    @Override
    public void shutdown() throws InternalErrorException {
        cameraModule.unsuscribe(this);
    }

    @Override
    public String getModuleInfo() {
        return "Ball tracking Module";
    }

    @Override
    public String getModuleVersion() {
        return "v0.1";
    }

    @Override
    public void configureDetection(boolean detectRed, boolean detectBlue, boolean detectGreen) {
        dR = detectRed;
        dB = detectBlue;
        dG = detectGreen;
    }
}
