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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.IObjectRecognitionModule;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.RecognizedObject;
import com.mytechia.robobo.framework.hri.vision.tag.ITagListener;
import com.mytechia.robobo.framework.hri.vision.tag.ITagModule;
import com.mytechia.robobo.framework.hri.vision.tag.Tag;
import com.mytechia.robobo.framework.hri.vision.util.AuxPropertyWriter;
import com.mytechia.robobo.framework.hri.vision.util.CameraDistortionCalibrationData;
import com.mytechia.robobo.framework.service.RoboboServiceHelper;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.Board;
import org.opencv.aruco.CharucoBoard;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

public class TagCalibrationActivity extends AppCompatActivity implements ICameraListener, GestureDetector.OnGestureListener, ITagListener {
    private static final String TAG="CameraFaceTestActivity";



    private RoboboServiceHelper roboboHelper;
    private RoboboManager roboboManager;


    private ICameraModule camModule;
    private ITagModule arucoModule;
    private CameraBridgeViewBase bridgeBase;


    private ImageView imageView = null;
    private Button captureButton;
    private Button calibrateButton;
    private Switch visualizeSwitch;


    private List<RecognizedObject> objectList;
    List<Tag> markers;
    Mat ids;
    boolean detected = false;
    boolean preview = false;
    boolean capturing = false;

    Mat capturedImage;

    private AuxPropertyWriter propertyWriter;
    private CameraDistortionCalibrationData distortionData;

    List <Mat> capturedList;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"TouchEvent");

        return true;

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration_test);
        objectList = new ArrayList<RecognizedObject>();
        capturedList = new ArrayList<Mat>();
        propertyWriter = new AuxPropertyWriter();
        //Request permissions
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    4);
        }


        this.imageView = (ImageView) findViewById(R.id.testImageView) ;
        this.bridgeBase = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        this.calibrateButton = (Button) findViewById(R.id.calibrateButton);
        this.captureButton = (Button) findViewById(R.id.captureButton);
        this.visualizeSwitch = (Switch) findViewById(R.id.visualizeSwitch);



        this.captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturing = true;
            }
        });



        this.visualizeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preview = isChecked;

                if (isChecked){
                    arucoModule.resumeDetection();
                }else{
                    arucoModule.pauseDetection();
                }
            }
        });



        this.calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Mat> ids = new ArrayList<Mat>();
                List<Mat> corners = new ArrayList<Mat>();
                CharucoBoard board = CharucoBoard.create(11,8,25,14.5f, Aruco.getPredefinedDictionary(Aruco.DICT_4X4_1000));
                boolean first = true;

                for (Mat image : capturedList){
                    ArrayList<Mat> tagCorners = new ArrayList<Mat>();
                    Mat tagIds = new Mat();
                    Mat charucoCorners = new Mat();
                    Mat charucoIds = new Mat();

                    Aruco.detectMarkers(image,Aruco.getPredefinedDictionary(Aruco.DICT_4X4_1000),tagCorners, tagIds);
                    Aruco.interpolateCornersCharuco(tagCorners,tagIds,image,board,charucoCorners,charucoIds);
                    Mat auxmat = new Mat(1,44,tagCorners.get(0).type());
                    corners.add(charucoCorners);
                    ids.add(charucoIds);
                }

                Size imagesize = new Size(capturedList.get(0).cols(),capturedList.get(0).rows());






                Mat cameraMatrix = new Mat();
                Mat distCoeffs = new Mat();
                List<Mat> rvecs = new ArrayList<Mat>();
                List<Mat> tvecs = new ArrayList<Mat>();
                Aruco.calibrateCameraCharuco(corners,ids,board,imagesize,cameraMatrix,distCoeffs,rvecs,tvecs);
int a = 1;
                /*distortionData = new CameraDistortionCalibrationData(cameraMatrix,distCoeffs,n);
                propertyWriter.storeConf("distCoeffs",distortionData.getDistCoeffs());
                propertyWriter.storeConf("cameraMatrix",distortionData.getCameraMatrix());
                propertyWriter.commitConf();*/
            }
        });



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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bridgeBase.setVisibility(SurfaceView.VISIBLE);
                camModule.passOCVthings(bridgeBase);
                camModule.signalInit();

            }
        });
        camModule.suscribe(this);
        camModule.changeCamera();
        arucoModule.suscribe(this);
        camModule.setFps(40);

        roboboManager.setPowerManagementEnabled(false);

    }

    @Override
    public void onNewFrame(final Frame frame) {

    }

    @Override
    public void onNewMat(Mat mat) {
        Imgproc.cvtColor(mat,mat,Imgproc.COLOR_BGRA2BGR);
        Mat newmat = mat.clone();

        if (preview){
            //newmat = drawArucos(markers, newmat);
            newmat = drawCharuco(newmat);

            detected = false;

        }

        if (capturing) {
            capturing = false;
            Mat mat_aux = new Mat();
            //capturedImage = new Mat();
            mat.copyTo(mat_aux);
            capturedList.add(mat_aux);
        }

        final Frame frame = new Frame(newmat);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(frame.getBitmap());

            }
        });
    }


    private  Mat drawArucos(List<Tag> tags, Mat image){

        for (Tag tag : tags){
            for (int i = 0; i < 4; i++){
                Imgproc.line(image, new Point(tag.getCorner(i).x,tag.getCorner(i).y),new Point(tag.getCorner((i+1)%4).x,tag.getCorner((i+1)%4).y),new Scalar(255,0,0));

                Imgproc.circle(image,new Point(tag.getCorner(i).x,tag.getCorner(i).y),3, new Scalar(0,255,0));
            }
        }

        return image;
    }
    private  Mat drawCharuco(Mat image){

        ArrayList<Mat> tagCorners = new ArrayList<Mat>();
        Mat tagIds = new Mat();
        Mat charucoCorners = new Mat();
        Mat charucoIds = new Mat();
        CharucoBoard board = CharucoBoard.create(11,8,25,14.5f, Aruco.getPredefinedDictionary(Aruco.DICT_4X4_1000));

        Aruco.detectMarkers(image,Aruco.getPredefinedDictionary(Aruco.DICT_4X4_1000),tagCorners, tagIds);
        Aruco.interpolateCornersCharuco(tagCorners,tagIds,image,board,charucoCorners,charucoIds);
        Aruco.drawDetectedCornersCharuco(image, charucoCorners);
        return image;
    }

    @Override
    public void onDebugFrame(final Frame frame,final String frameId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //imageView.setImageDrawable(new BitmapDrawable(getResources(), frame.getBitmap()));

            }
        });
    }

    @Override
    public void onOpenCVStartup() {

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
        camModule.changeCamera();

        return false;
    }

    @Override
    public void onAruco(List<Tag> markers) {
        this.markers = markers;
        detected = true;
    }


   /* @Override
    public void onAruco(List<Mat> corners, Mat ids) {
        this.corners = corners;

        this.ids = ids;
        this.detected = true;
    }*/
}
