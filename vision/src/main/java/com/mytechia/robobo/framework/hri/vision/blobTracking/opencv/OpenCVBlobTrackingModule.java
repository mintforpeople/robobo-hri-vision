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
import com.mytechia.robobo.framework.hri.vision.blobTracking.Blobcolor;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//http://www.pyimagesearch.com/2015/09/14/ball-tracking-with-opencv/
//https://github.com/badlogic/opencv-fun/blob/master/src/pool/utils/BallDetector.java

public class OpenCVBlobTrackingModule extends ABlobTrackingModule implements ICameraListener {

    private static final int POOL_SIZE=4;

    private ICameraModule cameraModule;

    private boolean firstFrame = true;

    private List<BlockTracker> blockTrackings= new ArrayList<>();

    private final Object lockBlockTrackings= new Object();

    private ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);

    private final Object lockWorkers = new Object();

    //Para evitar la creacion de muchos objetos
    private LinkedList<BlockTrackerWorker> workersPool= new LinkedList<>();



    public OpenCVBlobTrackingModule(){

        for (int i = 0; i <POOL_SIZE; i++) {
            workersPool.add(new BlockTrackerWorker(this));
        }

    }


    void returnToWorkersPool(BlockTrackerWorker blockTrackingWorker){

        if(blockTrackingWorker==null){
            return;
        }

        synchronized (lockWorkers) {

            if (!this.workersPool.contains(blockTrackingWorker)) {
                this.workersPool.add(blockTrackingWorker);
            }
        }
    }


    BlockTrackerWorker popWorkerFromPool(){

        synchronized (lockWorkers) {

            if(workersPool.isEmpty()){
                return null;
            }

            BlockTrackerWorker blockTrackingWorker=workersPool.pop();

            return blockTrackingWorker;
        }

    }

    @Override
    public void onNewFrame(Frame frame) {

        if (firstFrame) {
            resolutionX = frame.getWidth();
            resolutionY = frame.getHeight();
        }

    }

    @Override
    public void onNewMat(Mat mat) {

        if(mat.empty()){
            return;
        }

        synchronized (lockBlockTrackings) {

            for (BlockTracker blockTracking : blockTrackings) {

                if(blockTracking.capturing()){
                    continue;
                }

                BlockTrackerWorker blockTrackingWorker = this.popWorkerFromPool();

                if (blockTrackingWorker == null) {
                    continue;
                }

                blockTrackingWorker.configure(blockTracking, mat);

                threadPool.execute(blockTrackingWorker);

            }
        }


    }


    @Override
    public void onDebugFrame(Frame frame, String frameId) {}


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
        threadPool.shutdown();
    }

    @Override
    public String getModuleInfo() {
        return "Ball tracking Module";
    }

    @Override
    public String getModuleVersion() {
        return "v0.1";
    }

    private boolean existBlockTracking(Blobcolor blobcolor){
        for (BlockTracker blockTracker : blockTrackings) {
                if(blockTracker.getBlobcolor()==blobcolor){
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
                if((!existBlockTracking(Blobcolor.RED))) {
                    this.blockTrackings.add(new BlockTracker(new Size(11, 11), Blobcolor.RED));
                }
            } else {
                removeBlockTracking(Blobcolor.RED);
            }

            if (detectBlue) {
                if((!existBlockTracking(Blobcolor.BLUE))) {
                    this.blockTrackings.add(new BlockTracker(new Size(7, 7), Blobcolor.BLUE));
                }
            } else {
                removeBlockTracking(Blobcolor.BLUE);
            }

            if (detectGreen) {
                if((!existBlockTracking(Blobcolor.GREEN))) {
                    this.blockTrackings.add(new BlockTracker(new Size(11, 11), Blobcolor.GREEN));
                }
            } else {
                removeBlockTracking(Blobcolor.GREEN);
            }

            if (detectCustom) {
                if((!existBlockTracking(Blobcolor.CUSTOM))) {
                    this.blockTrackings.add(new BlockTracker(new Size(11, 11), Blobcolor.CUSTOM));
                }
            } else {
                removeBlockTracking(Blobcolor.CUSTOM);
            }
        }

    }

    private void removeBlockTracking(Blobcolor block){

        BlockTracker blockTrackingToRemove= null;

        for (BlockTracker blockTracking :this.blockTrackings) {
            if(blockTracking.getBlobcolor().equals(block)){
                blockTrackingToRemove= blockTracking;
            }
        }

        this.blockTrackings.remove(blockTrackingToRemove);


    }

    @Override
    public void setThreshold(int threshold) {

        for (BlockTracker blockTracking :this.blockTrackings) {
            blockTracking.setLostBlockThreshold(threshold);
        }
    }
}
