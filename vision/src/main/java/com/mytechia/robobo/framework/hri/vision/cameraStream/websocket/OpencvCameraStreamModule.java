package com.mytechia.robobo.framework.hri.vision.cameraStream.websocket;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.cameraStream.ACameraStreamModule;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class OpencvCameraStreamModule extends ACameraStreamModule implements ICameraListener {

    //Queue
    private ProcessWithQueue processFrameQueue;
    private LinkedBlockingQueue<byte[]> frameQueue;


    private boolean processing = false;

    ExecutorService executor;

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {

        m = manager;

        // Load camera and remote control modules
        try {
            cameraModule = m.getModuleInstance(ICameraModule.class);

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

    }

}
