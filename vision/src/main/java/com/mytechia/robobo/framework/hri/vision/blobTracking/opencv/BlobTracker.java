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

import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mytechia.robobo.framework.hri.vision.blobTracking.opencv.BlobTracker.DETECTION_STATE.DETECTED;
import static com.mytechia.robobo.framework.hri.vision.blobTracking.opencv.BlobTracker.DETECTION_STATE.NONE;


/**
 * Created by julio on 7/08/17.
 */
public class BlobTracker {

    private String TAG = "BlobTracker";

    public static final int RADIUS = 10;

    public static final double CIRCULARITY = 0.70;

    public static final double QUADRANGULARITY = 0.75;

    private final Blobcolor blobcolor;

    private int noDetectionCount = 0;

    private int lostBlobThreshold = 5;

    private final Size size;

    private int min_area = 1000;


    private Blob blob;

    public enum DETECTION_STATE {NONE, DETECTED, TEMP_DISSAPEAR,  DISSAPEAR}

    private DETECTION_STATE detectionState=NONE;

    private boolean processing = false;

    private Rect trackWindow = new Rect();

    private TermCriteria termCriteria = new TermCriteria(TermCriteria.EPS | TermCriteria.COUNT, 1, 0);



    public BlobTracker(Size size, Blobcolor blobcolor) {

        this.size = size;
        this.blobcolor = blobcolor;
    }

    public Blobcolor getBlobcolor() {
        return blobcolor;
    }

    public boolean processing() {
        return processing;
    }

    public void setLostBlobThreshold(int lostBlobThreshold) {
        this.lostBlobThreshold = lostBlobThreshold;
    }

    public void process(Mat mat) {
        double area = 0;
        Mat hsvFrame = new Mat();
        Mat backproj = new Mat();
        Imgproc.cvtColor(mat,hsvFrame,Imgproc.COLOR_BGR2HSV);

        //try {

            if (trackWindow.area()<= 1) {
                trackWindow = initTrackWindow(hsvFrame, blobcolor.getHistogramData());

            }
            if (trackWindow.area()> min_area) {
                backproj = calcBackproj(hsvFrame, blobcolor.getHistogramData());
                RotatedRect trackBox = Video.CamShift(backproj, trackWindow,
                        termCriteria);
                trackWindow = trackBox.boundingRect();

                //input = backproj;
                //Imgproc.threshold(input, input, (double) 2, (double) 255, Imgproc.THRESH_BINARY);
                area = Math.round(trackBox.size.area()*Math.PI/4);

                this.blob = new Blob(blobcolor, trackBox.center, (int) area, false, false);

            }
            else
            {
                trackWindow = new Rect();
                this.blob = null;
            }






        if (this.blob == null) {
            noDetectionCount += 1;

            switch (detectionState) {
                case DETECTED:
                    detectionState = DETECTION_STATE.TEMP_DISSAPEAR;
                    break;
                case TEMP_DISSAPEAR:
                    if (noDetectionCount > lostBlobThreshold){
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

        processing = false;

        hsvFrame.release();

    }

    public Blob detectedBlob() {
        return this.blob;
    }

    public DETECTION_STATE blobDetectionState() {
        return detectionState;
    }


    /**
     * Calculates the back projection image
     * @param input Input image
     * @param hist Back projection histogram
     * @return The output probability image
     */
    private Mat calcBackproj(Mat input, Mat hist) {

        MatOfFloat mRanges = new MatOfFloat(0, 179, 0, 255);
        MatOfInt mChannels = new MatOfInt(0, 1);

        List<Mat> lHSV = Arrays.asList(input);

        Mat backproj = new Mat();
        Imgproc.calcBackProject(lHSV, mChannels, hist, backproj, mRanges, 1
        );

        return backproj;
    }

    /**
     * Calculates the tracking window for camshift
     * @param input Input image
     * @param hist Back projection histogram
     * @return Tracking window rect
     */
    private Rect initTrackWindow(Mat input, Mat hist) {
        Mat backproj = calcBackproj(input, hist);
        Imgproc.threshold(backproj, backproj, (double) 2, (double) 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();

        try {
            Imgproc.findContours(backproj, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        } catch (Exception e) {
            e.printStackTrace();
            processing = false;
        }

        double maxarea = 0;
        MatOfPoint maxcontour = null;
        for (MatOfPoint c : contours) {
            if (Imgproc.contourArea(c) > maxarea) {
                maxcontour = c;
                maxarea = Imgproc.contourArea(c);
            }
        }
        Rect out = new Rect();

        if (maxarea > min_area)
            out = Imgproc.boundingRect(maxcontour);
        return out;
    }

}
