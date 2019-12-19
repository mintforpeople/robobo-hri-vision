package com.mytechia.robobo.framework.hri.vision.aruco.opencv;

import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.aruco.AArucoModule;
import com.mytechia.robobo.framework.hri.vision.aruco.ArucoTag;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;


import org.opencv.aruco.Aruco;
import org.opencv.aruco.DetectorParameters;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_BACK;
import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

public class OpencvArucoModule extends AArucoModule implements ICameraListener {

    private RoboboManager m;
    private ICameraModule cameraModule;

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        m = manager;
        // Load camera and remote control modules
        try {
            cameraModule = m.getModuleInstance(ICameraModule.class);

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }
        cameraModule.suscribe(this);
    }

    @Override
    public void shutdown() throws InternalErrorException {

    }

    @Override
    public String getModuleInfo() {
        return null;
    }

    @Override
    public String getModuleVersion() {
        return null;
    }

    @Override
    public void onNewFrame(Frame frame) {

    }

    @Override
    public void onNewMat(Mat mat) {
        Mat markerIds = new Mat();
        if (cameraModule.getCameraCode() == CAMERA_ID_FRONT){
            Core.flip(mat,mat,0);

        }
        ArrayList<Mat> markerCorners = new ArrayList<>();
        ArrayList<Mat> rejectedCandidates = new ArrayList<>();
        Imgproc.cvtColor(mat,mat,Imgproc.COLOR_BGRA2BGR);
        DetectorParameters parameters = DetectorParameters.create();
        parameters.set_minDistanceToBorder(0);
        parameters.set_adaptiveThreshWinSizeMax(100);
        Aruco.detectMarkers(mat, Aruco.getPredefinedDictionary(Aruco.DICT_4X4_1000),markerCorners,markerIds, parameters,rejectedCandidates);
        int i = 0;

        List<ArucoTag>  tags = new ArrayList<>();
        for (i = 0; i < markerIds.rows(); i++){

            ArucoTag tag = new ArucoTag(markerCorners.get(i),markerIds.get(i,0)[0]);
            Log.w("ARUCO", tag.toString());
        }

        if (markerIds.rows() > 0){
            notifyMarkersDetected(markerCorners, markerIds);
        }
        //Log.w("ARUCO", "Markers: "+markerIds.toString());

    }

    @Override
    public void onDebugFrame(Frame frame, String frameId) {

    }

    @Override
    public void onOpenCVStartup() {

    }
}
