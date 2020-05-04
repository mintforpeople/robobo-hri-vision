/*******************************************************************************
 * Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 * Copyright 2016 Luis Llamas <luis.llamas@mytechia.com>
 * <p>
 * This file is part of Robobo Sensing Modules.
 * <p>
 * Robobo Sensing Modules is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Robobo Orientation Module is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Robobo Sensing Modules.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.mytechia.robobo.framework.vision;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.laneDetection.ILaneDetectionListener;
import com.mytechia.robobo.framework.hri.vision.laneDetection.ILaneDetectionModule;
import com.mytechia.robobo.framework.hri.vision.laneDetection.Line;
import com.mytechia.robobo.framework.hri.vision.laneDetection.opencv.OpencvLaneDetectionModule;
import com.mytechia.robobo.framework.hri.vision.lineDetection.ILineDetectionListener;
import com.mytechia.robobo.framework.hri.vision.lineDetection.ILineDetectionModule;
import com.mytechia.robobo.framework.hri.vision.util.AuxPropertyWriter;
import com.mytechia.robobo.framework.service.RoboboServiceHelper;
//import com.mytechia.robobo.rob.BluetoothRobInterfaceModule;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

public class LaneDetectionActivity extends AppCompatActivity implements ICameraListener, ILaneDetectionListener, GestureDetector.OnGestureListener {
    private static final String TAG = "LaneDetectionActivity";


    private GestureDetectorCompat mDetector;

    private RoboboServiceHelper roboboHelper;
    private RoboboManager roboboManager;


    private ICameraModule camModule;
    private ILaneDetectionModule laneModule;
    private ILineDetectionModule lineModule;
    private CameraBridgeViewBase bridgeBase;


    private RelativeLayout rellayout = null;
    private TextView textView = null;
    private SurfaceView surfaceView = null;
    private ImageView imageView = null;
    private TextureView textureView = null;


    private Mat actualMat;
    private Mat mask;
    private Mat lines;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "TouchEvent");
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);

//Request permissions
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)/*||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                        != PackageManager.FEATURE_BLUETOOTH)*/) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    4);
        }

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.imageView = (ImageView) findViewById(R.id.testImageView);
        this.bridgeBase = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        this.textView = (TextView) findViewById(R.id.textView2);

        roboboHelper = new RoboboServiceHelper(this, new RoboboServiceHelper.Listener() {
            @Override
            public void onRoboboManagerStarted(RoboboManager robobo) {
                //the robobo service and manager have been started up
                roboboManager = robobo;

                //start the "custom" robobo application
                startRoboboApplication();
            }

            @Override
            public void onError(Throwable errorMsg) {
                final String error = errorMsg.getLocalizedMessage();
                Log.e(TAG, error);
            }

        });


        //start & bind the Robobo service
        Bundle options = new Bundle();
//        options.putString(BluetoothRobInterfaceModule.ROBOBO_BT_NAME_OPTION, "ROB-23R");
        roboboHelper.bindRoboboService(options);
    }

    private void startRoboboApplication() {

        mDetector = new GestureDetectorCompat(getApplicationContext(), this);
        try {
            this.camModule = this.roboboManager.getModuleInstance(ICameraModule.class);
            this.laneModule = this.roboboManager.getModuleInstance(ILaneDetectionModule.class);
            this.lineModule = this.roboboManager.getModuleInstance(ILineDetectionModule.class);

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }

        camModule.suscribe(this);
        laneModule.suscribe(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bridgeBase.setVisibility(SurfaceView.VISIBLE);
                camModule.passOCVthings(bridgeBase);
                camModule.signalInit();

            }
        });
        camModule.setFps(30);

    }

    @Override
    public void onNewFrame(final Frame frame) {

    }

    @Override
    public void onNewMat(Mat mat) {
        actualMat = mat.clone();
//        if (lines == null) return;
        if (mask == null) return;
//        if (camModule.getCameraCode() == CAMERA_ID_FRONT)
//            Core.flip(mat, mat, 1);
//        for (int x = 0; x < lines.rows(); x++) {
//            double[] l = lines.get(x, 0);
//            Imgproc.line(mat, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
//        }
        Core.addWeighted(mask, 1., mat, 0.5, 0, mat);

        final Frame frame = new Frame(mat);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(frame.getBitmap());
            }
        });
    }

    @Override
    public void onDebugFrame(final Frame frame, final String frameId) {
    }

    @Override
    public void onOpenCVStartup() {
        lines = new Mat();
        camModule.changeCamera();
//        Log.e(TAG,"Camera changed");

    }


    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        Log.w(TAG, "Fling!");
        camModule.changeCamera();

        return true;
    }


    public void onLane(double slope_left, double bias_left, double slope_right, double bias_right) {
        if (actualMat == null)
            return;
        // now separately draw solid lines to highlight them
        Mat line_mask = Mat.zeros(actualMat.size(), actualMat.type());
        // Left line
        Imgproc.line(line_mask, new Point(-bias_left / slope_left, 0), new Point((line_mask.rows() - 1 - bias_left) / slope_left, line_mask.rows() - 1), new Scalar(255, 0, 0), 10);
        // Right line
        Imgproc.line(line_mask, new Point(-bias_right / slope_right, 0), new Point((line_mask.rows() - 1 - bias_right) / slope_right, line_mask.rows() - 1), new Scalar(0, 0, 255), 10);
        mask = line_mask;
    }

    @Override
    public void onLane(Line line_lt, Line line_rt, Mat minv) {
        if (actualMat == null)
            return;

        // now separately draw solid lines to highlight them
        Mat line_warp = Mat.zeros(actualMat.size(), actualMat.type());
        if (line_lt.detected)
            line_lt.draw(line_warp, new Scalar(255, 0, 0), 20, false);// average=keep_state)
        if (line_rt.detected)
            line_rt.draw(line_warp, new Scalar(0, 0, 255), 20, false);// average=keep_state)
        Imgproc.warpPerspective(line_warp, line_warp, minv, actualMat.size());
        mask = line_warp;

    }

}
