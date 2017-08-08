package com.mytechia.robobo.framework.hri.vision.blobTracking.opencv;

import android.util.Log;

import org.opencv.core.Mat;

import java.util.Objects;

import static com.mytechia.robobo.framework.hri.vision.blobTracking.opencv.BlockTracker.DETECTION_STATE.DETECTED;
import static com.mytechia.robobo.framework.hri.vision.blobTracking.opencv.BlockTracker.DETECTION_STATE.DISSAPEAR;

/**
 * Created by julio on 8/08/17.
 */
public class BlockTrackerWorker implements Runnable {

    private String TAG = "BlockTracker";

    private Mat mat;
    private BlockTracker blockTracking;
    private OpenCVBlobTrackingModule openCVBlobTrackingModule;


    public BlockTrackerWorker(OpenCVBlobTrackingModule openCVBlobTrackingModule) {

        Objects.requireNonNull(openCVBlobTrackingModule, "The parameter openCVBlobTrackingModule is requiered");

        this.openCVBlobTrackingModule = openCVBlobTrackingModule;
    }


    public void configure(BlockTracker blockTracking, Mat mat) {

        this.blockTracking = blockTracking;

        this.mat = mat;

    }


    public BlockTracker getBlockTracking() {
        return blockTracking;
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
            Log.e(TAG, "Error running BlockTrackerWorker", th);
        }finally {
            this.openCVBlobTrackingModule.returnToWorkersPool(this);
        }

    }


}
