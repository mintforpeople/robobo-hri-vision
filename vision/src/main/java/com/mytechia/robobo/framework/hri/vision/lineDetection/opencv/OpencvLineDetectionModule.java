package com.mytechia.robobo.framework.hri.vision.lineDetection.opencv;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListenerV2;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.lineDetection.ALineDetectionModule;
import com.mytechia.robobo.framework.hri.vision.util.AuxPropertyWriter;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

public class OpencvLineDetectionModule extends ALineDetectionModule implements ICameraListenerV2 {

    private ICameraModule cameraModule;
    private boolean processing = false;
    private Size matSize = null;
    private ExecutorService executor;
    private AuxPropertyWriter propertyWriter;
    private float[] tl = new float[2],
            tr = new float[2],
            bl = new float[2],
            br = new float[2];

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        propertyWriter = new AuxPropertyWriter("camera.properties", manager);
        tl[0] = Float.parseFloat(propertyWriter.retrieveConf("lt_tl_x"));
        tl[1] = Float.parseFloat(propertyWriter.retrieveConf("lt_tl_y"));
        tr[0] = Float.parseFloat(propertyWriter.retrieveConf("lt_tr_x"));
        tr[1] = Float.parseFloat(propertyWriter.retrieveConf("lt_tr_y"));
        bl[0] = Float.parseFloat(propertyWriter.retrieveConf("lt_bl_x"));
        bl[1] = Float.parseFloat(propertyWriter.retrieveConf("lt_bl_y"));
        br[0] = Float.parseFloat(propertyWriter.retrieveConf("lt_br_x"));
        br[1] = Float.parseFloat(propertyWriter.retrieveConf("lt_br_y"));

        m = manager;
        // Load camera and remote control modules
        try {
            cameraModule = m.getModuleInstance(ICameraModule.class);
            rcmodule = m.getModuleInstance(IRemoteControlModule.class);


        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }

        executor = Executors.newFixedThreadPool(1);

        rcmodule.registerCommand("START-LINE", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                startDetection();
            }
        });

        rcmodule.registerCommand("START-LINE-STATS", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                status = true;
            }
        });
        rcmodule.registerCommand("STOP-LINE-STATS", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                status = false;
            }
        });

        rcmodule.registerCommand("STOP-LINE", new ICommandExecutor() {
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
    public void onNewMatV2(final Mat mat, final int frame_id, long timestamp) {

        if (!processing && mat.cols() > 0 && mat.rows() > 0) {
            // Execute on its own thread to avoid locking the camera callback
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    processing = true;
                    matSize = mat.size();
                    Mat lines = new Mat();
                    doCanny(mat);

//                    if (useMask)
                    doSegment(mat);

                    Imgproc.HoughLinesP(mat,
                            lines,
                            2,
                            Math.PI / 180,
                            100,
                            100,
                            50);

                    notifyLinesDetected(lines, frame_id);

                    processing = false;
                }
            });

        }

    }

    private void doSegment(Mat mat) {
        int height = mat.rows(), width = mat.cols();
        Mat mask = Mat.zeros(mat.rows(), mat.cols(), mat.type());

        MatOfPoint mPoints = new MatOfPoint();
        List<MatOfPoint> polygons = new ArrayList<>();
        Point[] points = new Point[4];


        points[0] = new Point((int) (width * bl[0]), (int) (height * bl[1]));
        points[1] = new Point((int) (width * br[0]), (int) (height * br[1]));
        points[2] = new Point((int) (width * tr[0]), (int) (height * tr[1]));
        points[3] = new Point((int) (width * tl[0]), (int) (height * tl[1]));


        mPoints.fromArray(points);
        polygons.add(mPoints);

        Imgproc.fillPoly(mask, polygons, new Scalar(255, 255, 255));
        Core.bitwise_and(mat, mask, mat);
    }

    private void doCanny(Mat mat) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 0);
        Imgproc.Canny(mat, mat, 50, 150);
    }

    @Override
    public void onOpenCVStartup() {
    }

    @Override
    public void pauseDetection() {
        cameraModule.unsuscribe(this);
    }

    @Override
    public void resumeDetection() {
        cameraModule.suscribe(this);
    }

    public Size getMatSize() {
        return matSize;
    }
}
