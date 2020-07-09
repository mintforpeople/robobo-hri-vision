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


import android.os.Bundle;
import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.blobTracking.ABlobTrackingModule;
import com.mytechia.robobo.framework.hri.vision.blobTracking.Blobcolor;
import com.mytechia.robobo.framework.hri.vision.util.CameraCalibrationData;
import com.mytechia.robobo.framework.hri.vision.util.ColorCalibrationDataHSV;
import com.mytechia.robobo.framework.hri.vision.util.ColorCalibrationDataHistogram;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;


/**
 * Opencv implementation of the blob detector
 */
public class OpenCVBlobTrackingModule extends ABlobTrackingModule implements ICameraListener {
    //http://www.pyimagesearch.com/2015/09/14/ball-tracking-with-opencv/
//https://github.com/badlogic/opencv-fun/blob/master/src/pool/utils/BallDetector.java
    private static final int POOL_SIZE=4;

    private ICameraModule cameraModule;

    private boolean firstFrame = true;

    private List<BlobTracker> blobTrackings = new ArrayList<>();

    private final Object lockBlockTrackings= new Object();

    private ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);

    private final Object lockWorkers = new Object();

    //Para evitar la creacion de muchos objetos y, por consiguiente, la recolecion de basura
    private LinkedList<BlobTrackerWorker> workersPool= new LinkedList<>();



    public OpenCVBlobTrackingModule(){
        // Add workers to pool
        for (int i = 0; i <POOL_SIZE; i++) {
            workersPool.add(new BlobTrackerWorker(this));
        }

    }


    void returnToWorkersPool(BlobTrackerWorker blockTrackingWorker){

        if(blockTrackingWorker==null){
            return;
        }

        synchronized (lockWorkers) {

            if (!this.workersPool.contains(blockTrackingWorker)) {
                this.workersPool.add(blockTrackingWorker);
            }
        }
    }


    BlobTrackerWorker popWorkerFromPool(){

        synchronized (lockWorkers) {

            if(workersPool.isEmpty()){
                return null;
            }

            BlobTrackerWorker blockTrackingWorker=workersPool.pop();

            return blockTrackingWorker;
        }

    }

    @Override
    public void onNewFrame(Frame frame) {

        // In the first frame get the resolution of the captured images
        if (firstFrame) {
            resolutionX = frame.getWidth();
            resolutionY = frame.getHeight();
        }

    }

    @Override
    public void onNewMat(Mat mat) {
        //TODO: add a "processing" flag to discard new mats if processing one already
        if(mat.empty()){
            return;
        }

        synchronized (lockBlockTrackings) {

            // For each color being tracked
            for (BlobTracker blobTracking : blobTrackings) {

                if(blobTracking.processing()){
                    continue;
                }

                BlobTrackerWorker blobTrackingWorker = this.popWorkerFromPool();

                if (blobTrackingWorker == null) {
                    continue;
                }

                // Configure tracker with the new mat
                blobTrackingWorker.configure(blobTracking, mat);
                //Execute workers
                threadPool.execute(blobTrackingWorker);

            }
        }


    }


    @Override
    public void onDebugFrame(Frame frame, String frameId) {}

    @Override
    public void onOpenCVStartup() {

        // Load calibration values stored in the manager options
        Bundle opts = m.getOptions();
        CameraCalibrationData data = null;
        try {

            data = (CameraCalibrationData) opts.getSerializable("cameraCalibrationData");

            ColorCalibrationDataHistogram col = (ColorCalibrationDataHistogram) data.getBlue();
            BLUE_CAL = new Blobcolor(col.getHistMat(),"BLUE");
            col = (ColorCalibrationDataHistogram) data.getGreen();
            GREEN_CAL = new Blobcolor(col.getHistMat(),"GREEN");
            col = (ColorCalibrationDataHistogram) data.getRed();
            RED_CAL = new Blobcolor(col.getHistMat(),"RED");
            col = (ColorCalibrationDataHistogram) data.getCustom();
            CUSTOM_CAL = new Blobcolor(col.getHistMat(),"CUSTOM");

        }catch (NullPointerException e){
            m.log(TAG,"No calibration data found, using defaults");
        }catch (Exception e){
            e.printStackTrace();
        }

        // Register remote command to configure different trackings
        rcmodule.registerCommand("CONFIGURE-BLOBTRACKING", new ICommandExecutor() {
                @Override
                public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                    configureDetection(
                            Boolean.parseBoolean(c.getParameters().get("red")),
                            Boolean.parseBoolean(c.getParameters().get("green")),
                            Boolean.parseBoolean(c.getParameters().get("blue")),
                            Boolean.parseBoolean(c.getParameters().get("custom")));
                }
        });
    }


    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        m = manager;
        Log.w("BLOB","STARTUP BLOB");
        // Get instances od camera and remote modules
        try {
            cameraModule = m.getModuleInstance(ICameraModule.class);
            rcmodule = m.getModuleInstance(IRemoteControlModule.class);

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }
        cameraModule.suscribe(this);

    }

    @Override
    public void shutdown() throws InternalErrorException {
        cameraModule.unsuscribe(this);
        threadPool.shutdown();
    }

    @Override
    public String getModuleInfo() {
        return "Blob tracking Module";
    }

    @Override
    public String getModuleVersion() {
        return "v0.1";
    }

    private boolean existBlobTracking(Blobcolor blobcolor){
        for (BlobTracker blobTracker : blobTrackings) {
                if(blobTracker.getBlobcolor().equals(blobcolor)){
                    return true;
                }
        }

        return false;
    }



    @Override
    public void configureDetection(boolean detectRed,
                                   boolean detectGreen,
                                   boolean detectBlue,
                                   boolean detectCustom) {


        synchronized (lockBlockTrackings) {
            if (detectRed) {
                if((!existBlobTracking(RED_CAL))) {
                    this.blobTrackings.add(new BlobTracker(new Size(11, 11), RED_CAL));
                }
            } else {
                removeBlobTracking(RED_CAL);
            }

            if (detectBlue) {
                if((!existBlobTracking(BLUE_CAL))) {
                    this.blobTrackings.add(new BlobTracker(new Size(11, 11), BLUE_CAL));
                }
            } else {
                removeBlobTracking(BLUE_CAL);
            }

            if (detectGreen) {
                if((!existBlobTracking(GREEN_CAL))) {
                    this.blobTrackings.add(new BlobTracker(new Size(11, 11), GREEN_CAL));
                }
            } else {
                removeBlobTracking(GREEN_CAL);
            }

            if (detectCustom) {
                if((!existBlobTracking(CUSTOM_CAL))) {
                    this.blobTrackings.add(new BlobTracker(new Size(11, 11), CUSTOM_CAL));
                }
            } else {
                removeBlobTracking(CUSTOM_CAL);
            }
        }

    }

    private void removeBlobTracking(Blobcolor block){

        BlobTracker blockTrackingToRemove= null;

        for (BlobTracker blockTracking :this.blobTrackings) {
            if(blockTracking.getBlobcolor().equals(block)){
                blockTrackingToRemove= blockTracking;
            }
        }

        this.blobTrackings.remove(blockTrackingToRemove);


    }

    @Override
    public void setThreshold(int threshold) {

        synchronized (lockBlockTrackings) {
            for (BlobTracker blockTracking : this.blobTrackings) {
                blockTracking.setLostBlobThreshold(threshold);
            }
        }
    }
}
