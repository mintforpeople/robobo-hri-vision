package com.mytechia.robobo.framework.hri.vision.tag.opencv;

import android.os.Environment;
import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.tag.ATagModule;
import com.mytechia.robobo.framework.hri.vision.tag.Tag;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.util.AuxPropertyWriter;
import com.mytechia.robobo.framework.hri.vision.util.CameraDistortionCalibrationData;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;


import org.opencv.aruco.Aruco;
import org.opencv.aruco.CharucoBoard;
import org.opencv.aruco.DetectorParameters;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

public class OpencvTagModule extends ATagModule implements ICameraListener {

    private RoboboManager m;
    private ICameraModule cameraModule;
    //private List<String> rvecs;
    //private List<String> tvecs;
    private int currentTagDict = Aruco.DICT_4X4_1000;
    private CameraDistortionCalibrationData calibrationData;
    private AuxPropertyWriter propertyWriter;

    private boolean processing = false;

    ExecutorService executor;



    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {


        m = manager;
        propertyWriter = new AuxPropertyWriter("camera.properties", manager);
        // Load camera and remote control modules
        try {
            cameraModule = m.getModuleInstance(ICameraModule.class);
            rcmodule = m.getModuleInstance(IRemoteControlModule.class);


        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }

        executor = Executors.newFixedThreadPool(1);

        rcmodule.registerCommand("START-TAG", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                startDetection();
            }
        });

        rcmodule.registerCommand("STOP-TAG", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                stopDetection();

            }
        });

        startDetection();

    }

    private void stopDetection() {
        cameraModule.unsuscribe(this);
    }

    private void startDetection() {
        cameraModule.suscribe(this);
    }

    @Override
    public void shutdown() throws InternalErrorException {
        stopDetection();
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
    public void onNewMat(final Mat mat) {

        if (!processing && mat.cols() > 0 && mat.rows() > 0) {
            // Execute on its own thread to avoid locking the camera callback
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    processing = true;
                    try {
                        Mat markerIds = new Mat();

                        // If the camera is the frontal the image is mirrored
                        if (cameraModule.getCameraCode() == CAMERA_ID_FRONT) {
                            Core.flip(mat, mat, 1);

                        }

                        ArrayList<Mat> markerCorners = new ArrayList<>();
                        ArrayList<Mat> rejectedCandidates = new ArrayList<>();

                        // Colorspace conversion
                        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);

                        // Detection parameters
                        DetectorParameters parameters = DetectorParameters.create();
                        parameters.set_minDistanceToBorder(0);
                        parameters.set_adaptiveThreshWinSizeMax(100);

                        // Marker detection
                        Aruco.detectMarkers(mat, Aruco.getPredefinedDictionary(currentTagDict), markerCorners, markerIds, parameters, rejectedCandidates, calibrationData.getCameraMatrixMat(), calibrationData.getDistCoeffsMat());

                        // Rotation vector
                        Mat rvecs = new Mat();
                        // Translation vector
                        Mat tvecs = new Mat();

                        if (markerIds.rows() > 0) {
                            // rvecs, tvecs, 3x1 CV_64FC3 matrix
                            // Marker pose detection
                            Aruco.estimatePoseSingleMarkers(markerCorners, 100, calibrationData.getCameraMatrixMat(), calibrationData.getDistCoeffsMat(), tvecs, rvecs);

                            // rvecs, tvecs, 3x1 CV_64FC1 matrix
                            //Aruco.estimatePoseBoard(markerCorners,markerIds,board,calibrationData.getCameraMatrixMat(),calibrationData.getDistCoeffsMat(),rvecs,tvecs);

                            // List of detected tags
                            List<Tag> tags = new ArrayList<>();

                            // Individual vectors for the tags
                            double[] tagRvecs = new double[3];
                            double[] tagTvecs = new double[3];

                            for (int i = 0; i < markerIds.rows(); i++) {
                                Tag tag;

                                tagRvecs[0] = rvecs.get(i, 0)[0];
                                tagRvecs[1] = rvecs.get(i, 0)[1];
                                tagRvecs[2] = rvecs.get(i, 0)[2];
                                tagTvecs[0] = tvecs.get(i, 0)[0];
                                tagTvecs[1] = tvecs.get(i, 0)[1];
                                tagTvecs[2] = tvecs.get(i, 0)[2];

                                // Check the camera before creating the tags
                                if (cameraModule.getCameraCode() == CAMERA_ID_FRONT) {
                                    //tag = new Tag(markerCorners.get(i), markerIds.get(i, 0)[0], true, cameraModule.getResX());
                                    // TODO: Revisar si se van a espejar las coordenadas o así está bien
                                    tag = new Tag(markerCorners.get(i), markerIds.get(i, 0)[0], true, cameraModule.getResX(), tagRvecs, tagTvecs);
                                } else {
                                    //tag = new Tag(markerCorners.get(i), markerIds.get(i, 0)[0], false, cameraModule.getResX());
                                    tag = new Tag(markerCorners.get(i), markerIds.get(i, 0)[0], false, cameraModule.getResX(), tagRvecs, tagTvecs);
                                }
//                                Log.w("ARUCO", Arrays.toString(tag.getRMat()));
                                tags.add(tag);
                            }

                            // Notify to the remote control module
                            notifyMarkersDetected(tags);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    processing = false;
                }
            });

        }

    }

    @Override
    public void onDebugFrame(Frame frame, String frameId) {

    }

    @Override
    public void onOpenCVStartup() {
        calibrationData = new CameraDistortionCalibrationData(
                propertyWriter.retrieveConf("cameraMatrix" + cameraModule.getCameraCode(), propertyWriter.retrieveConf("cameraMatrix")),
                propertyWriter.retrieveConf("distCoeffs" + cameraModule.getCameraCode(), propertyWriter.retrieveConf("distCoeffs")));
    }

    @Override
    public void useAruco() {
        currentTagDict = Aruco.DICT_4X4_1000;
    }

    @Override
    public void useAprilTags() {
        currentTagDict = Aruco.DICT_APRILTAG_16h5;
    }

    @Override
    public void pauseDetection() {
        cameraModule.unsuscribe(this);
    }

    @Override
    public void resumeDetection() {
        cameraModule.suscribe(this);
    }
}
