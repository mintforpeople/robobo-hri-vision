package com.mytechia.robobo.framework.hri.vision.cameraStream.websocket;

import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.cameraStream.ACameraStreamModule;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

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
    private static final String TAG = "OpenCVCameraStreamModule";

    IRemoteControlModule rcmodule;

    //Queue
    private ProcessWithQueue processFrameQueue;
    private LinkedBlockingQueue<byte[]> frameQueue;


    // FPS control variables
    private long lastFrameTime = 0;
    private long deltaTimeThreshold = 17;

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

        rcmodule.registerCommand("START-STREAM", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                cameraModule.suscribe(OpencvCameraStreamModule.this);
            }
        });

        rcmodule.registerCommand("STOP-STREAM", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                cameraModule.unsuscribe(OpencvCameraStreamModule.this);
            }
        });

        rcmodule.registerCommand("SET-STREAM-FPS", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                if(c.getParameters().containsKey("fps")){
                    setFps(Integer.parseInt(c.getParameters().get("fps")));
                }
            }
        });

        server = new Server();
        startServer();
    }

    public void setFps(int fps) {
        deltaTimeThreshold = 1000/fps;
    }

    private void startServer() {
        server.start();

        frameQueue = new LinkedBlockingQueue<>();
        processFrameQueue = new ProcessWithQueue(frameQueue);
    }

    @Override
    public void shutdown() {
        server.close();
        cameraModule.unsuscribe(this);
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
        long millis = System.currentTimeMillis();

        if (!processing && millis-lastFrameTime>=deltaTimeThreshold) {

            lastFrameTime = millis;
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
