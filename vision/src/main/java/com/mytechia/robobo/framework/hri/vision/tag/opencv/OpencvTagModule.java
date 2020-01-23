package com.mytechia.robobo.framework.hri.vision.tag.opencv;

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


import org.opencv.aruco.Aruco;
import org.opencv.aruco.CharucoBoard;
import org.opencv.aruco.DetectorParameters;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

public class OpencvTagModule extends ATagModule implements ICameraListener {

    private RoboboManager m;
    private ICameraModule cameraModule;
    private String distCoeffs = "{\"rows\"\\:1,\"cols\"\\:5,\"type\"\\:0,\"data\"\\:\"AQAAAAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\\\u003d\\\\u003d\\\\n\"}";
    private String cameraMatrix = "{\"rows\"\\:3,\"cols\"\\:3,\"type\"\\:0,\"data\"\\:\"/wDtAP//AAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\\\nAAAAAAAAAAAAAAAAAAAA\\\\n\"}";
    //private List<String> rvecs;
    //private List<String> tvecs;
    private int currentTagDict = Aruco.DICT_4X4_1000;
    private CameraDistortionCalibrationData calibrationData;
    private AuxPropertyWriter propertyWriter;
    private CharucoBoard board ;

    private boolean processing = false;

    ExecutorService executor;

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {


        m = manager;
        propertyWriter = new AuxPropertyWriter();
        // Load camera and remote control modules
        try {
            cameraModule = m.getModuleInstance(ICameraModule.class);

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }

        distCoeffs = propertyWriter.retrieveConf("distCoeffs", distCoeffs);
        cameraMatrix = propertyWriter.retrieveConf("cameraMatrix", cameraMatrix);
        /*rvecs.set(0, propertyWriter.retrieveConf("rvecs_0","{\"rows\"\\:3,\"cols\"\\:1,\"type\"\\:0,\"data\"\\:\"AwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\\\n\"}"));
        rvecs.set(1, propertyWriter.retrieveConf("rvecs_1","{\"rows\"\\:3,\"cols\"\\:1,\"type\"\\:0,\"data\"\\:\"AwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\\\n\"}"));
        rvecs.set(2, propertyWriter.retrieveConf("rvecs_2","{\"rows\"\\:3,\"cols\"\\:1,\"type\"\\:0,\"data\"\\:\"AwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\\\\n\"}"));
        tvecs.set(0, propertyWriter.retrieveConf("tvecs_0","{\"rows\"\\:3,\"cols\"\\:1,\"type\"\\:0,\"data\"\\:\"ALv/AAAAAAAAAAAAAAAAAAAAAAAAAAAA\\\\n\"}"));
        tvecs.set(1, propertyWriter.retrieveConf("tvecs_1","{\"rows\"\\:3,\"cols\"\\:1,\"type\"\\:0,\"data\"\\:\"ALv/AAAAAAAAAAAAAAAAAAAAAAAAAAAA\\\\n\"}"));
        tvecs.set(2, propertyWriter.retrieveConf("tvecs_2","{\"rows\"\\:3,\"cols\"\\:1,\"type\"\\:0,\"data\"\\:\"Cs3/AAAAAAAAAAAAAAAAAAAAAAAAAAAA\\\\n\"}"));*/
        calibrationData = new CameraDistortionCalibrationData(cameraMatrix,distCoeffs);//,rvecs,tvecs);
        cameraModule.suscribe(this);

        executor = Executors.newFixedThreadPool(1);

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
    public void onNewMat(final Mat mat) {

        if (!processing) {
            // Execute on its own thread to avoid locking the camera callback
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    processing = true;

                    Mat markerIds = new Mat();

                    // If the camera is the frontal the image is mirrored
                    if (cameraModule.getCameraCode() == CAMERA_ID_FRONT) {
                        Core.flip(mat, mat, 0);

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

                    // rvecs, tvecs, 3x1 CV_64FC3 matrix
                    // Marker pose detection
                    Aruco.estimatePoseSingleMarkers(markerCorners, 14.5f, calibrationData.getCameraMatrixMat(), calibrationData.getDistCoeffsMat(), tvecs, rvecs);

                    // rvecs, tvecs, 3x1 CV_64FC1 matrix
                    //Aruco.estimatePoseBoard(markerCorners,markerIds,board,calibrationData.getCameraMatrixMat(),calibrationData.getDistCoeffsMat(),rvecs,tvecs);
                    /*if (tvecs.rows()>0){

                        Log.w("RVECS", (float)rvecs.get(0,0)[0]+" "+(float)rvecs.get(1,0)[0]+ " "+(float) rvecs.get(2,0)[0]);
                    }*/

                    // List of detected tags
                    List<Tag> tags = new ArrayList<>();

                    // Individual vectors for the tags
                    float[] tagRvecs = new float[3];
                    float[] tagTvecs = new float[3];

                    for (int i = 0; i < markerIds.rows(); i++) {
                        Tag tag;

                        tagRvecs[0] = (float) rvecs.get(i, 0)[0];
                        tagRvecs[1] = (float) rvecs.get(i, 0)[1];
                        tagRvecs[2] = (float) rvecs.get(i, 0)[2];
                        tagTvecs[0] = (float) tvecs.get(i, 0)[0];
                        tagTvecs[1] = (float) tvecs.get(i, 0)[1];
                        tagTvecs[2] = (float) tvecs.get(i, 0)[2];

                        // Check the camera before creating the tags
                        if (cameraModule.getCameraCode() == CAMERA_ID_FRONT) {
                            //tag = new Tag(markerCorners.get(i), markerIds.get(i, 0)[0], true, cameraModule.getResX());
                            tag = new Tag(markerCorners.get(i),markerIds.get(i,0)[0],true, cameraModule.getResX(),tagRvecs,tagTvecs);

                        } else {
                            //tag = new Tag(markerCorners.get(i), markerIds.get(i, 0)[0], false, cameraModule.getResX());
                            tag = new Tag(markerCorners.get(i),markerIds.get(i,0)[0],false, cameraModule.getResX(),tagRvecs,tagTvecs);
                        }
                        //Log.w("ARUCO", tag.toString());
                        tags.add(tag);
                    }

                    // Notify to the remote control module
                    if (markerIds.rows() > 0) {
                        notifyMarkersDetected(tags);
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
        //board = CharucoBoard.create(11,8,25,14.5f, Aruco.getPredefinedDictionary(Aruco.DICT_4X4_1000));

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
