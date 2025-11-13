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

import org.opencv.core.CvType;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.DetectorParameters;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.Dictionary;
import org.opencv.objdetect.Objdetect;
import org.opencv.objdetect.RefineParameters;
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
    private Dictionary dictionary;
    private CameraDistortionCalibrationData calibrationData;
    private AuxPropertyWriter propertyWriter;

    private ArucoDetector detector;
    private boolean processing = false;
    private boolean stopped = false;

    private List<Tag> lastTags = new ArrayList<Tag>();
    private List<Tag> currentTags = new ArrayList<Tag>();

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
        // Uncomment to start with the module active
        startDetection();

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

                        dictionary = Objdetect.getPredefinedDictionary(currentTagDict);
                        // Colorspace conversion
                        Imgproc.cvtColor(clonedMat, clonedMat, Imgproc.COLOR_BGRA2BGR);
                        // Detection parameters
                        DetectorParameters parameters = new DetectorParameters();
                        parameters.set_minDistanceToBorder(3);
                        parameters.set_cornerRefinementMethod(Objdetect.CORNER_REFINE_SUBPIX);
                        //parameters.set_cornerRefinementWinSize(15);
                        parameters.set_adaptiveThreshWinSizeMax(100);

                        detector = new ArucoDetector(dictionary, parameters);

                        // Marker detection
                        //detector.detectMarkers(clonedMat, Aruco.getPredefinedDictionary(currentTagDict), markerCorners, markerIds, parameters, rejectedCandidates, calibrationData.getCameraMatrixMat(), calibrationData.getDistCoeffsMat());
                        // We don't use the calibration data here? Only in refine detectedMarkers ???
                        detector.detectMarkers(clonedMat, markerCorners, markerIds, rejectedCandidates);

                        // List of detected tags
                        lastTags = currentTags;
                        currentTags = new ArrayList<Tag>();

                        //Log.d("ARUCODETECT", markerIds.rows() + "");
                        if (markerIds.rows() > 0) {
                            // rvecs, tvecs, 3x1 CV_64FC3 matrix
                            // Marker pose detection
                            Mat cameraMatrix = new Mat();
                            MatOfDouble distCoeffs = new MatOfDouble();

                            calibrationData.getCameraMatrixMat().convertTo(cameraMatrix, CvType.CV_32F);
                            calibrationData.getDistCoeffsMat().convertTo(distCoeffs, CvType.CV_32F);

                            MatOfPoint3f markerPoints = new MatOfPoint3f(
                                    new Point3(-markerLength / 2,  markerLength / 2, 0),
                                    new Point3( markerLength / 2,  markerLength / 2, 0),
                                    new Point3( markerLength / 2, -markerLength / 2, 0),
                                    new Point3(-markerLength / 2, -markerLength / 2, 0)
                            );

                            for (int i = 0; i < markerIds.rows(); i++) {
                                Tag tag;

                                Mat tagRvecs = Mat.zeros(3, 1, CvType.CV_64F);
                                Mat tagTvecs = Mat.zeros(3, 1, CvType.CV_64F);

                                float[] data = new float[8]; // 4 points * 2 channels
                                markerCorners.get(i).get(0, 0, data);
                                MatOfPoint2f imageCorners = new MatOfPoint2f(
                                        new Point(data[0], data[1]),
                                        new Point(data[2], data[3]),
                                        new Point(data[4], data[5]),
                                        new Point(data[6], data[7])
                                );

                                Calib3d.solvePnP(
                                        markerPoints,
                                        imageCorners,
                                        cameraMatrix,
                                        distCoeffs,
                                        tagRvecs,
                                        tagTvecs,
                                        false,
                                        Calib3d.SOLVEPNP_IPPE_SQUARE
                                );

                                double[] rvecArray = new double[3];
                                tagRvecs.get(0, 0, rvecArray);

                                double[] tvecArray = new double[3];
                                tagRvecs.get(0, 0, tvecArray);

                                // Check the camera before creating the tags
                                if (cameraModule.getCameraCode() == CAMERA_ID_FRONT) {
                                    //tag = new Tag(markerCorners.get(i), markerIds.get(i, 0)[0], true, cameraModule.getResX());
                                    // TODO: Revisar si se van a espejar las coordenadas o así está bien
                                    tag = new Tag(markerCorners.get(i), markerIds.get(i, 0)[0], true, cameraModule.getResX(), rvecArray, tvecArray);
                                } else {
                                    //tag = new Tag(markerCorners.get(i), markerIds.get(i, 0)[0], false, cameraModule.getResX());
                                    tag = new Tag(markerCorners.get(i), markerIds.get(i, 0)[0], false, cameraModule.getResX(), rvecArray, tvecArray);
                                }
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

        propertyWriter = AuxPropertyWriter.getInstance(m);
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
    }

    @Override
    public void useAprilTags() {
        currentTagDict = Objdetect.DICT_APRILTAG_16h5;
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

    public static void estimatePoseSingleMarkers(
            List<Mat> corners,
            double markerSize,
            Mat cameraMatrix,
            MatOfDouble distortionCoeffs,
            List<Mat> rvecs,
            List<Mat> tvecs) {

        // Define marker 3D corner points (same order as OpenCV's ArUco)
        MatOfPoint3f markerPoints = new MatOfPoint3f(
                new Point3(-markerSize / 2,  markerSize / 2, 0),
                new Point3( markerSize / 2,  markerSize / 2, 0),
                new Point3( markerSize / 2, -markerSize / 2, 0),
                new Point3(-markerSize / 2, -markerSize / 2, 0)
        );

        // Clear previous values if lists are reused
        rvecs.clear();
        tvecs.clear();

        // Estimate pose for each detected marker
        for (Mat c : corners) {
            Mat rvec = new Mat();
            Mat tvec = new Mat();

            boolean success = Calib3d.solvePnP(
                    markerPoints,
                    new MatOfPoint2f(c),
                    cameraMatrix,
                    distortionCoeffs,
                    rvec,
                    tvec,
                    false,
                    Calib3d.SOLVEPNP_IPPE_SQUARE
            );

            if (!success) {
                // Fallback to zero vectors if pose estimation fails
                rvec = Mat.zeros(3, 1, CvType.CV_64F);
                tvec = Mat.zeros(3, 1, CvType.CV_64F);
            }

            rvecs.add(rvec);
            tvecs.add(tvec);
        }
    }
}
