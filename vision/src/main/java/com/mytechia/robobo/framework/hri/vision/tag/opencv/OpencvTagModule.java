package com.mytechia.robobo.framework.hri.vision.tag.opencv;

import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListenerV2;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.tag.ATagModule;
import com.mytechia.robobo.framework.hri.vision.tag.Tag;
import com.mytechia.robobo.framework.hri.vision.util.AuxPropertyWriter;
import com.mytechia.robobo.framework.hri.vision.util.CameraDistortionCalibrationData;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.*;
import org.opencv.objdetect.*;
import org.opencv.aruco.Aruco;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.ximgproc.FastLineDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

public class OpencvTagModule extends ATagModule implements ICameraListenerV2 {

    ExecutorService executor;
    private float markerLength = 100;
    private RoboboManager m;
    private ICameraModule cameraModule;
    //private List<String> rvecs;
    //private List<String> tvecs;
    private int currentTagDict = Objdetect.DICT_4X4_100;
    private CameraDistortionCalibrationData calibrationData;
    private AuxPropertyWriter propertyWriter;
    private boolean processing = false;
    private boolean stopped = false;

    private List<Tag> lastTags = new ArrayList<Tag>();
    private List<Tag> currentTags = new ArrayList<Tag>();

    private ArucoDetector arucoDetector;
    private DetectorParameters detectorParameters;



    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {


        m = manager;
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
        rcmodule.registerCommand("CHANGE-SIZE-TAG", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                markerLength = Integer.parseInt(c.getParameters().get("size"));
            }
        });
        // Detection parameters
        detectorParameters = new DetectorParameters();
        detectorParameters.set_minDistanceToBorder(3);
        detectorParameters.set_cornerRefinementMethod(Objdetect.CORNER_REFINE_SUBPIX);
        //parameters.set_cornerRefinementWinSize(15);
        detectorParameters.set_adaptiveThreshWinSizeMax(100);
        arucoDetector = new ArucoDetector(Objdetect.getPredefinedDictionary(currentTagDict), detectorParameters);

        // Uncomment to start with the module active
        //startDetection();

    }

    private void stopDetection() {
        stopped = true;
        cameraModule.unsuscribe(this);
    }

    private void startDetection() {
        stopped = false;
        cameraModule.suscribe(this);

    }

    @Override
    public void shutdown() throws InternalErrorException {
        stopDetection();
    }

    @Override
    public String getModuleInfo() {
        return "Aruco Tag Module";
    }

    @Override
    public String getModuleVersion() {
        return "null";
    }

    @Override
    public void onNewMatV2(final Mat mat, final int frameId, long timestamp) {

        if (!stopped && !processing && mat.cols() > 0 && mat.rows() > 0) {
            // Execute on its own thread to avoid locking the camera callback
//            Log.d("TAG","TAGFRAME");
            executor.execute(new Runnable() {
                    @Override
                    public void run() {
                    processing = true;

                    Mat clonedMat = mat.clone();

                    if (cameraModule.getCameraCode() != calibrationData.cameraCode)
                        loadCalibrationData();

                    try {
                        Mat markerIds = new Mat();

                        // If the camera is the frontal the image is mirrored
                        if (cameraModule.getCameraCode() == CAMERA_ID_FRONT) {
                            Core.flip(clonedMat, clonedMat, 1);

                        }

                        ArrayList<Mat> markerCorners = new ArrayList<>();
                        ArrayList<Mat> rejectedCandidates = new ArrayList<>();

                        // Colorspace conversion
                        Imgproc.cvtColor(clonedMat, clonedMat, Imgproc.COLOR_BGRA2BGR);

                        // Marker detection
                        arucoDetector.detectMarkers(clonedMat, markerCorners, markerIds, rejectedCandidates);

                        // Rotation vector
                        Mat rvecs = new Mat();
                        // Translation vector
                        Mat tvecs = new Mat();

                        // List of detected tags
                        lastTags = currentTags;
                        currentTags = new ArrayList<Tag>();

                        if (markerIds.rows() > 0) {
                            // rvecs, tvecs, 3x1 CV_64FC3 matrix
                            // Marker pose detection
                            Aruco.estimatePoseSingleMarkers(markerCorners, markerLength, calibrationData.getCameraMatrixMat(), calibrationData.getDistCoeffsMat(), rvecs, tvecs);

                            // rvecs, tvecs, 3x1 CV_64FC1 matrix
                            // Aruco.estimatePoseBoard(markerCorners,markerIds,board,calibrationData.getCameraMatrixMat(),calibrationData.getDistCoeffsMat(),rvecs,tvecs);

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
                                currentTags.add(tag);
                            }
                            // Notify to the remote control module
                            notifyMarkersDetected(currentTags, frameId);
                            clonedMat.release();
                        }

                        // Check if the tags have changed
                        List<Tag> lostTags = lastTags.stream()
                                .filter(tag1 -> currentTags.stream().noneMatch(tag2 -> tag2.getId() == tag1.getId()))
                                .collect(Collectors.toList());

                        List<Tag> newTags = currentTags.stream()
                                .filter(tag1 -> lastTags.stream().noneMatch(tag2 -> tag2.getId() == tag1.getId()))
                                .collect(Collectors.toList());

                        for (Tag lostTag : lostTags) {
                            notifyMarkerDisappear(lostTag, frameId);
                        }

                        for (Tag newTag : newTags) {
                            notifyMarkerAppear(newTag, frameId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        clonedMat.release();
                    }

                    processing = false;
                }
            });

        }

    }

    @Override
    public void onOpenCVStartup() {

        propertyWriter = new AuxPropertyWriter(m.getApplicationContext(), "camera", m);
        loadCalibrationData();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                stopDetection();
            }
        });

    }

    private void loadCalibrationData() {
        calibrationData = new CameraDistortionCalibrationData(
                propertyWriter.retrieveConf("cameraMatrix" + cameraModule.getCameraCode(), propertyWriter.retrieveConf("cameraMatrix")),
                propertyWriter.retrieveConf("distCoeffs" + cameraModule.getCameraCode(), propertyWriter.retrieveConf("distCoeffs")));
        calibrationData.cameraCode = cameraModule.getCameraCode();

    }

    @Override
    public void useAruco() {
        currentTagDict = Objdetect.DICT_4X4_1000;
        arucoDetector.setDictionary(Objdetect.getPredefinedDictionary(currentTagDict));
    }

    @Override
    public void useAprilTags() {
        currentTagDict = Objdetect.DICT_APRILTAG_16h5;
        arucoDetector.setDictionary(Objdetect.getPredefinedDictionary(currentTagDict));
    }

    @Override
    public void pauseDetection() {
        stopped = true;
        cameraModule.unsuscribe(this);
    }

    @Override
    public void resumeDetection() {
        stopped = false;
        cameraModule.suscribe(this);
    }
}
