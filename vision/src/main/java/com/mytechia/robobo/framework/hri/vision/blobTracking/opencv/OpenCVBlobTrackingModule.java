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
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.blobTracking.ABlobTrackingModule;
import com.mytechia.robobo.framework.hri.vision.blobTracking.Blob;
import com.mytechia.robobo.framework.hri.vision.blobTracking.Blobcolor;
import com.mytechia.robobo.framework.hri.vision.util.FrameCounter;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

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
    private boolean processing = false;
    private boolean dR = true;
    private boolean dG = false;
    private boolean dB = false;
    private boolean dC = false;
    private boolean firstFrame = true;
    private int noDetectionCountR;
    private int noDetectionCountG;
    private int noDetectionCountB;
    private int noDetectionCountC;
    private boolean blobDissapearR;
    private boolean blobDissapearG;
    private boolean blobDissapearB;
    private boolean blobDissapearC;
    public int LOST_THRESHOLD = 5;


    private FrameCounter fps = new FrameCounter();

    @Override
    public void onNewFrame(Frame frame) {
        if (firstFrame) {
            resolutionX = frame.getWidth();
            resolutionY = frame.getHeight();
        }

    }

    @Override
    public void onNewMat(Mat mat) {
        if (!processing) {
            processing = true;

            int detectionsR = 0;
            int detectionsG = 0;
            int detectionsB = 0;
            int detectionsC = 0;

            Mat hsvFrame = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);
            Imgproc.cvtColor(mat, hsvFrame, Imgproc.COLOR_BGR2HSV);
            Imgproc.blur(hsvFrame, hsvFrame, new Size(11, 11));

            if (dR) {
                detectionsR = configureTrackingBlob(mat, detectionsR, hsvFrame, new Size(11, 11), Blobcolor.RED);

                if (detectionsR == 0) {
                    noDetectionCountR += 1;
                    if ((noDetectionCountR > LOST_THRESHOLD) && (!blobDissapearR)) {
                        notifyBlobDissapear(Blobcolor.RED);
                        blobDissapearR = true;
                    }
                } else {
                    noDetectionCountR = 0;
                    blobDissapearR = false;
                }
            }

            if (dG) {

                detectionsG = configureTrackingBlob(mat, detectionsG, hsvFrame, new Size(7, 7), Blobcolor.GREEN);

                if (detectionsG == 0) {
                    noDetectionCountG += 1;
                    if ((noDetectionCountG > LOST_THRESHOLD) && (!blobDissapearG)) {
                        notifyBlobDissapear(Blobcolor.GREEN);
                        blobDissapearG = true;
                    } else {
                        noDetectionCountG = 0;
                        blobDissapearG = false;
                    }
                }

                if (dB) {

                    detectionsB = configureTrackingBlob(mat, detectionsB, hsvFrame, new Size(11, 11), Blobcolor.BLUE);

                    if (detectionsB == 0) {
                        noDetectionCountB += 1;
                        if ((noDetectionCountB > LOST_THRESHOLD) && (!blobDissapearB)) {
                            notifyBlobDissapear(Blobcolor.BLUE);
                            blobDissapearB = true;
                        }
                    } else {
                        noDetectionCountB = 0;
                        blobDissapearB = false;
                    }
                }

                if (dC) {
                    detectionsC = configureTrackingBlob(mat, detectionsC, hsvFrame, new Size(11, 11), Blobcolor.CUSTOM);
                    if (detectionsC == 0) {
                        noDetectionCountC += 1;
                        if ((noDetectionCountC > LOST_THRESHOLD) && (!blobDissapearC)) {
                            notifyBlobDissapear(Blobcolor.CUSTOM);
                            blobDissapearC = true;
                        } else {
                            noDetectionCountC = 0;
                            blobDissapearC = false;
                        }
                    }


                    processing = false;

                }
            }

        }
    }

    private int configureTrackingBlob(Mat mat, int detections, Mat hsvFrame, Size size, Blobcolor blobcolor) {
        Mat maskred = new Mat(mat.rows(), mat.cols(), CvType.CV_32F);

        Core.inRange(hsvFrame, Blobcolor.getLowRange(blobcolor), Blobcolor.getHighRange(blobcolor), maskred);

        //Clean mask and find contours
        Imgproc.erode(maskred, maskred, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, size));
        Imgproc.dilate(maskred, maskred, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, size));
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
            double area = Imgproc.contourArea(maxcontour);

            double circularity = area / (Math.PI * radius[0] * radius[0]);
            if (radius[0] > 10) {
                detections = detections + 1;

                Blob b = null;
                if (circularity > 0.70) {
                    b = new Blob(blobcolor, center, (int) radius[0], true, false);
                } else {
                    RotatedRect rrect = Imgproc.minAreaRect(maxcontourf);
                    double quadrangularity = area / rrect.size.area();
                    if (quadrangularity > 0.75) {

                        b = new Blob(blobcolor, center, (int) area, false, true);
                    } else {
                        b = new Blob(blobcolor, center, (int) area, false, false);
                    }
                }

                this.notifyTrackingBlob(b);
            }

        }
        return detections;
    }


    @Override
    public void onDebugFrame(Frame frame, String frameId) {

    }


    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        m = manager;
        cameraModule = m.getModuleInstance(ICameraModule.class);
        rcmodule = m.getModuleInstance(IRemoteControlModule.class);
        cameraModule.suscribe(this);
        rcmodule.registerCommand("CONFIGUREBLOB", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                configureDetection(Boolean.parseBoolean(c.getParameters().get("red")),
                        Boolean.parseBoolean(c.getParameters().get("green")),
                        Boolean.parseBoolean(c.getParameters().get("blue")),
                        Boolean.parseBoolean(c.getParameters().get("custom")));
            }
        });
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
    public void configureDetection(boolean detectRed, boolean detectGreen,
                                   boolean detectBlue, boolean detectCustom) {
        dR = detectRed;
        dB = detectBlue;
        dG = detectGreen;
        dC = detectCustom;
    }

    @Override
    public void setThreshold(int th) {
        LOST_THRESHOLD = th;
    }
}
