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
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.tag.ITagListener;
import com.mytechia.robobo.framework.hri.vision.tag.ITagModule;
import com.mytechia.robobo.framework.hri.vision.tag.Tag;
import com.mytechia.robobo.framework.hri.vision.util.AuxPropertyWriter;
import com.mytechia.robobo.framework.hri.vision.util.CameraDistortionCalibrationData;
import com.mytechia.robobo.framework.service.RoboboServiceHelper;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.aruco.Aruco;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

public class TagDetectActivity extends AppCompatActivity implements ICameraListener, GestureDetector.OnGestureListener, ITagListener {
    private static final String TAG = "CameraFaceTestActivity";
    List<Tag> markers;
    boolean detected = false;
    private GestureDetectorCompat mDetector;
    private RoboboServiceHelper roboboHelper;
    private RoboboManager roboboManager;
    private ICameraModule camModule;
    private ITagModule arucoModule;
    private CameraBridgeViewBase bridgeBase;
    private TextView textView = null;
    private ImageView imageView = null;
    private CameraDistortionCalibrationData calibrationData;
    AuxPropertyWriter propertyWriter;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "TouchEvent");
        this.mDetector.onTouchEvent(event);
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
                        != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    4);
        }

        //this.surfaceView = (SurfaceView) findViewById(R.id.testSurfaceView);
        this.imageView = findViewById(R.id.testImageView);
        this.bridgeBase = findViewById(R.id.HelloOpenCvView);
        this.textView = findViewById(R.id.textView2);

//        this.textureView = (TextureView) findViewById(R.id.textureView);
        roboboHelper = new RoboboServiceHelper(this, new RoboboServiceHelper.Listener() {
            @Override
            public void onRoboboManagerStarted(RoboboManager robobo) {
                //the robobo service and manager have been started up
                roboboManager = robobo;
                //dismiss the wait dialog
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
        roboboHelper.bindRoboboService(options);
    }

    private void startRoboboApplication() {

        try {

            this.camModule = this.roboboManager.getModuleInstance(ICameraModule.class);
            this.arucoModule = this.roboboManager.getModuleInstance(ITagModule.class);


        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }

        camModule.suscribe(this);
        arucoModule.suscribe(this);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                bridgeBase.setVisibility(SurfaceView.VISIBLE);
                camModule.passOCVthings(bridgeBase);

                camModule.signalInit();
            }
        });
        mDetector = new GestureDetectorCompat(getApplicationContext(), this);


    }

    @Override
    public void onNewFrame(final Frame frame) {

    }

    @Override
    public void onNewMat(Mat mat) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);
        Mat newmat = mat.clone();
        Core.flip(newmat,newmat,1);

        if (detected) {
            //Aruco.drawDetectedMarkers(newmat, corners, ids);

            if (camModule.getCameraCode() != calibrationData.cameraCode)
                loadCalibrationData();
            if (camModule.getCameraCode() == CAMERA_ID_FRONT) {

                newmat = drawArucos(markers, newmat);


            } else {

                newmat = drawArucos(markers, newmat);

            }

            detected = false;

        }

        final Frame frame = new Frame(newmat);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(frame.getBitmap());

            }
        });
    }

    private void loadCalibrationData() {
        calibrationData = new CameraDistortionCalibrationData(
                propertyWriter.retrieveConf("cameraMatrix" + camModule.getCameraCode(), propertyWriter.retrieveConf("cameraMatrix")),
                propertyWriter.retrieveConf("distCoeffs" + camModule.getCameraCode(), propertyWriter.retrieveConf("distCoeffs")));
        calibrationData.cameraCode = camModule.getCameraCode();

    }


    private Mat drawArucos(List<Tag> tags, Mat image) {

        for (Tag tag : tags) {
            for (int i = 0; i < 4; i++) {
                //
                //Imgproc.line(image, new Point(tag.getCorner(i).x, tag.getCorner(i).y), new Point(tag.getCorner((i + 1) % 4).x, tag.getCorner((i + 1) % 4).y), new Scalar(255, 0, 0),3);
//
                //Imgproc.circle(image, new Point(tag.getCorner(i).x, tag.getCorner(i).y), 6, new Scalar(0, 255, 0));

                Mat rvecs = new Mat(1,1,CvType.CV_64FC3);
                Mat tvecs = new Mat(1,1,CvType.CV_64FC3);
                rvecs.put(0,0,tag.getRvecs());
                tvecs.put(0,0,tag.getTvecs());
                Aruco.drawAxis(image, calibrationData.getCameraMatrixMat(),calibrationData.getDistCoeffsMat(),rvecs, tvecs, 100 );
            }
        }

        return image;
    }

    @Override
    public void onDebugFrame(final Frame frame, final String frameId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //imageView.setImageDrawable(new BitmapDrawable(getResources(), frame.getBitmap()));

            }
        });
    }

    @Override
    public void onOpenCVStartup() {
        camModule.setFps(40);
//        ((ATagModule)arucoModule).useRosTypeStatus(true);
        propertyWriter = new AuxPropertyWriter("camera.properties", roboboManager);
        loadCalibrationData();
        arucoModule.resumeDetection();


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

                arucoModule.resumeDetection();


    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        camModule.changeCamera();

        return false;
    }

    @Override
    public void onAruco(List<Tag> markers) {
        this.markers = markers;
        Log.d("DETECTEDARUCO", markers.toString());
        detected = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bridgeBase != null)
            bridgeBase.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (bridgeBase != null)
            bridgeBase.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bridgeBase != null)
            bridgeBase.enableView();
    }


}
