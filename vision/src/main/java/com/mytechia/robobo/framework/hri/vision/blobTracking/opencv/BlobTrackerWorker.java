/*******************************************************************************
 *
 *   Copyright 2017 Mytech Ingenieria Aplicada <http://www.mytechia.com>
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

import org.opencv.core.Mat;

import java.util.Objects;

import static com.mytechia.robobo.framework.hri.vision.blobTracking.opencv.BlobTracker.DETECTION_STATE.DETECTED;
import static com.mytechia.robobo.framework.hri.vision.blobTracking.opencv.BlobTracker.DETECTION_STATE.DISSAPEAR;

/**
 * Created by julio on 8/08/17.
 */
public class BlobTrackerWorker implements Runnable {

    private String TAG = "BlobTracker";

    private Mat mat;
    private BlobTracker blockTracking;
    private OpenCVBlobTrackingModule openCVBlobTrackingModule;


    public BlobTrackerWorker(OpenCVBlobTrackingModule openCVBlobTrackingModule) {

        Objects.requireNonNull(openCVBlobTrackingModule, "The parameter openCVBlobTrackingModule is requiered");

        this.openCVBlobTrackingModule = openCVBlobTrackingModule;
    }


    public void configure(BlobTracker blockTracking, Mat mat) {

        this.blockTracking = blockTracking;

        this.mat = mat;

    }


    @Override
    public void run() {

        try {

            this.blockTracking.capture(mat);

            if (blockTracking.blodDetectionState() == DISSAPEAR) {

                openCVBlobTrackingModule.notifyBlobDissapear(blockTracking.getBlobcolor());

            } else if ((blockTracking.blodDetectionState() == DETECTED)) {

                openCVBlobTrackingModule.notifyTrackingBlob(blockTracking.detectedBlod());

            }

        }catch (Throwable th){
            Log.e(TAG, "Error running BlobTrackerWorker", th);
        }finally {
            this.openCVBlobTrackingModule.returnToWorkersPool(this);
        }

    }


}