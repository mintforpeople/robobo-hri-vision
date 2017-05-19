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
import com.mytechia.robobo.framework.hri.vision.blobTracking.ABlobTrackingModule;
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
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

//http://www.pyimagesearch.com/2015/09/14/ball-tracking-with-opencv/
//https://github.com/badlogic/opencv-fun/blob/master/src/pool/utils/BallDetector.java
public class OpenCVBlobTrackingModule extends ABlobTrackingModule implements ICameraListener {
    private ICameraModule cameraModule;
    @Override
    public void onNewFrame(Frame frame) {
        
    }

    @Override
    public void onNewMat(Mat mat) {

        Mat hsvFrame = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, hsvFrame, Imgproc.COLOR_BGR2HSV);
        Imgproc.blur(hsvFrame,hsvFrame, new Size(11,11));
        Mat mask= new Mat(mat.rows(), mat.cols(), CvType.CV_32F);

        Core.inRange(hsvFrame, Blobcolor.getLowRange(Blobcolor.RED ), Blobcolor.getHighRange(Blobcolor.RED),mask);
        Imgproc.erode(mask,mask,Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE,new Size(11,11)));
        Imgproc.dilate(mask,mask,Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE,new Size(11,11)));
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mask,contours,new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.size()>0){
            int maxarea = 0;
            MatOfPoint maxcontour = null;
            for (MatOfPoint c:contours){
                if (Imgproc.contourArea(c)> maxarea){
                    maxcontour = c;
                }
            }
            MatOfPoint2f maxcontourf = new MatOfPoint2f( maxcontour.toArray() );
            float[] radius = new float[1];
            Point center = new Point();
            Imgproc.minEnclosingCircle(maxcontourf,center,radius);
            if (radius[0]>10){
                this.notifyTrackingBall(Blobcolor.GREEN,(int)center.x, (int)center.y, (int)radius[0]);
            }
        }

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
}
