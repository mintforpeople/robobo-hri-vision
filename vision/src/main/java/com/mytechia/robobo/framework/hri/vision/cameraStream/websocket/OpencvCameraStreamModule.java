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
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class OpencvCameraStreamModule extends ACameraStreamModule implements ICameraListener {

    //Queue
    private ProcessWithQueue processFrameQueue;
    private LinkedBlockingQueue<byte[]> frameQueue;


    private boolean processing = false;

    ExecutorService executor;
    Server server;


    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {

        m = manager;
        executor = Executors.newFixedThreadPool(1);


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


        server = new Server();
        server.start();

        frameQueue = new LinkedBlockingQueue<>();
        processFrameQueue = new ProcessWithQueue(frameQueue);
    }

    @Override
    public void shutdown() throws InternalErrorException {
        server.close();
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
            processing = true;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2RGB);
                        MatOfByte bytemat = new MatOfByte();

                        Imgcodecs.imencode(".jpg", mat, bytemat);

                        // You can use something like this to lower the quality of the jpegs
                        //MatOfInt props = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 30);
                        //Imgcodecs.imencode(".jpg", mat, bytemat, props);

                        byte[] bytes = bytemat.toArray();

                        if (frameQueue.size() == 30)
                            frameQueue.take();

                        frameQueue.put(bytes);

                    } catch (InterruptedException e) {
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

    }

}
