package com.mytechia.robobo.framework.hri.vision.cameraStream.opencv;

import android.os.Handler;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.cameraStream.ACameraStreamModule;
import com.mytechia.robobo.framework.hri.vision.cameraStream.ProcessType;
import com.mytechia.robobo.framework.hri.vision.cameraStream.ProcessWithQueue;
import com.mytechia.robobo.framework.hri.vision.util.AuxPropertyWriter;
import com.mytechia.robobo.framework.hri.vision.util.CameraDistortionCalibrationData;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.aruco.Aruco;
import org.opencv.aruco.CharucoBoard;
import org.opencv.aruco.DetectorParameters;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

public class OpencvCameraStreamModule extends ACameraStreamModule implements ICameraListener {

    private RoboboManager m;
    private ICameraModule cameraModule;

    //Queue
    private ProcessWithQueue processFrameQueue;
    private LinkedBlockingQueue<byte[]> frameQueue;


    private int currentTagDict = Aruco.DICT_4X4_1000;
    private CameraDistortionCalibrationData calibrationData;
    private AuxPropertyWriter propertyWriter;
    private CharucoBoard board;

    private boolean processing = false;

    ExecutorService executor;

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {


        m = manager;
        propertyWriter = new AuxPropertyWriter();
        // Load camera and remote control modules
        try {
            cameraModule = m.getModuleInstance(ICameraModule.class);
            rcmodule = m.getModuleInstance(IRemoteControlModule.class);


        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }
        Properties defaults = new Properties();
        try {

            defaults.load(manager.getApplicationContext().getAssets().open("camproperties.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        cameraModule.suscribe(this);

        Server server = new Server();
        server.start();
        frameQueue = new LinkedBlockingQueue<>();
        processFrameQueue = new ProcessWithQueue(frameQueue);
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

//        if (!processing) {
//            // Execute on its own thread to avoid locking the camera callback
//
//            processing = true;


        // If the camera is the frontal the image is mirrored
//                    if (cameraModule.getCameraCode() == CAMERA_ID_FRONT) {
//                        Core.flip(mat, mat, 1);
//
//                    }

        try {
            if (frameQueue.size() == 30) {
                frameQueue.take();
            }
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2RGB);
            MatOfByte bytemat = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, bytemat);
            byte[] bytes = bytemat.toArray();
            frameQueue.put(bytes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


//                    // Colorspace conversion


//            processing = false;
//
//
//        }

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

    @Override
    public void startServer() {


    }
}
