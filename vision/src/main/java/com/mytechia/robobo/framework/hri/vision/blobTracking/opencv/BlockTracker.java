/*******************************************************************************
 *
 *   Copyright 2017 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2017 Luis Llamas <luis.llamas@mytechia.com>
 *   Copyright 2017 Julio Gomez <julio.gomez@mytechia.com>
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

import android.util.Log;

import com.mytechia.robobo.framework.hri.vision.blobTracking.Blob;
import com.mytechia.robobo.framework.hri.vision.blobTracking.Blobcolor;

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

import static com.mytechia.robobo.framework.hri.vision.blobTracking.opencv.BlockTracker.DETECTION_STATE.DETECTED;
import static com.mytechia.robobo.framework.hri.vision.blobTracking.opencv.BlockTracker.DETECTION_STATE.NONE;


/**
 * Created by julio on 7/08/17.
 */
public class BlockTracker {

    private String TAG = "BlockTracker";

    public static final int RADIUS = 10;

    public static final double CIRCULARITY = 0.70;

    public static final double QUADRANGULARITY = 0.75;

    private final Blobcolor blobcolor;

    private int noDetectionCount = 0;

    private int lostBlockThreshold = 5;

    private final Size size;

    private Blob blob;

    public enum DETECTION_STATE {NONE, DETECTED, TEMP_DISSAPEAR,  DISSAPEAR}

    private DETECTION_STATE detectionState=NONE;

    private boolean capturing = false;


    public BlockTracker(Size size, Blobcolor blobcolor) {

        this.size = size;
        this.blobcolor = blobcolor;
    }

    public Blobcolor getBlobcolor() {
        return blobcolor;
    }

    public boolean capturing() {
        return capturing;
    }

    public void setLostBlockThreshold(int lostBlockThreshold) {
        this.lostBlockThreshold = lostBlockThreshold;
    }

    public void capture(Mat mat) {

        capturing = true;

        Mat hsvFrame = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);

        Imgproc.cvtColor(mat, hsvFrame, Imgproc.COLOR_BGR2HSV);

        Imgproc.blur(hsvFrame, hsvFrame, new Size(11, 11));

        Mat mask = new Mat(mat.rows(), mat.cols(), CvType.CV_32F);

        Core.inRange(hsvFrame, Blobcolor.getLowRange(blobcolor), Blobcolor.getHighRange(blobcolor), mask);

        //Clean mask and find contours
        Imgproc.erode(mask, mask, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, size));

        Imgproc.dilate(mask, mask, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, size));

        List<MatOfPoint> contoursred = new ArrayList<>();

        try {
            Imgproc.findContours(mask, contoursred, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        } catch (Throwable th) {
            Log.e(TAG, "", th);
            capturing = false;
        }

        this.blob = null;

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
            float[] radii = new float[1];
            Point center = new Point();
            Imgproc.minEnclosingCircle(maxcontourf, center, radii);
            double area = Imgproc.contourArea(maxcontour);

            float radius = radii[0];
            double circularity = area / (Math.PI * radius * radius);

            if (radius > RADIUS) {

                if (circularity > CIRCULARITY) {
                    this.blob = new Blob(blobcolor, center, (int) radius, true, false);
                } else {
                    RotatedRect rrect = Imgproc.minAreaRect(maxcontourf);
                    double quadrangularity = area / rrect.size.area();
                    if (quadrangularity > QUADRANGULARITY) {
                        blob = new Blob(blobcolor, center, (int) area, false, true);
                    } else {
                        blob = new Blob(blobcolor, center, (int) area, false, false);
                    }
                }

            }
        }

        if (blob == null) {
            noDetectionCount += 1;

            switch (detectionState) {
                case DETECTED:
                    detectionState = DETECTION_STATE.TEMP_DISSAPEAR;
                    break;
                case TEMP_DISSAPEAR:
                    if (noDetectionCount > lostBlockThreshold){
                        detectionState = DETECTION_STATE.DISSAPEAR;
                    }
                    break;
                case DISSAPEAR:
                    detectionState=NONE;
                    break;
            }

        } else {
            noDetectionCount = 0;
            detectionState = DETECTED;
        }

        capturing = false;

        hsvFrame.release();

    }

    public Blob detectedBlod() {
        return this.blob;
    }

    public DETECTION_STATE blodDetectionState() {
        return detectionState;
    }


}
