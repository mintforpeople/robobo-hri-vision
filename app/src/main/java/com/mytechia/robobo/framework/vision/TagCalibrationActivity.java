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
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
    private static final String TAG = "CameraFaceTestActivity";

    private RoboboServiceHelper roboboHelper;
    private RoboboManager roboboManager;

    private ICameraModule camModule;
    private ITagModule arucoModule;
    private CameraBridgeViewBase bridgeBase;

    private ImageView imageView = null;
    private Button captureButton;
    private Button calibrateButton;
    private Button changeCameraButton;
    private Switch visualizeSwitch;
    private TextView imagesCounter;

    List<Tag> markers;
    boolean detected = false;
    boolean preview = false;
    boolean capturing = false;

    int squaresX = 11, squaresY = 8;
    //    int squaresX = 18, squaresY = 12;
    float squareLength = 25f, markerLength = 18.75f;
//    float squareLength = 14f, markerLength = 10f;

    private AuxPropertyWriter propertyWriter;
    private CameraDistortionCalibrationData distortionData;

    List<Mat> capturedList;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "TouchEvent");
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration_test);

        capturedList = new ArrayList<Mat>();

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

        // Set view variables with the layout
        this.imageView = (ImageView) findViewById(R.id.testImageView);
        this.bridgeBase = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        this.calibrateButton = (Button) findViewById(R.id.calibrateButton);
        this.captureButton = (Button) findViewById(R.id.captureButton);
        this.visualizeSwitch = (Switch) findViewById(R.id.visualizeSwitch);
        this.imagesCounter = (TextView) findViewById(R.id.imagesCounter);
        this.changeCameraButton = (Button) findViewById(R.id.changeCameraButton);



        roboboHelper = new RoboboServiceHelper(this, new RoboboServiceHelper.Listener() {
            @Override
            public void onRoboboManagerStarted(RoboboManager robobo) {
                //the robobo service and manager have been started up
                roboboManager = robobo;
                propertyWriter = new AuxPropertyWriter("camera.properties", robobo);

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

    private void calibrateCamera() {
        if (capturedList.size() == 0) {
            Toast.makeText(getApplicationContext(), "Please, take at least one calibration picture!", Toast.LENGTH_SHORT).show();
            return;
        }

        Mat cameraMatrix = new Mat();
        Mat distCoeffs = new Mat();
        List<Mat> rvecs = new ArrayList<Mat>();
        List<Mat> tvecs = new ArrayList<Mat>();


        List<Mat> ids = new ArrayList<Mat>();
        List<Mat> corners = new ArrayList<Mat>();
        int corners_count = 0;

        CharucoBoard board = CharucoBoard.create(squaresX, squaresY, squareLength, markerLength, Aruco.getPredefinedDictionary(Aruco.DICT_4X4_1000));
        boolean first = true;

        Size imageSize = new Size(camModule.getResX(),camModule.getResY());
        for (Mat image : capturedList) {
            ArrayList<Mat> tagCorners = new ArrayList<Mat>();
            Mat tagIds = new Mat();
            Mat charucoCorners = new Mat();
            Mat charucoIds = new Mat();

            Aruco.detectMarkers(image, Aruco.getPredefinedDictionary(Aruco.DICT_4X4_1000), tagCorners, tagIds);
            //Aruco.refineDetectedMarkers(image,board,tagCorners,tagIds);
            if (tagCorners.size() > 0)
                Aruco.interpolateCornersCharuco(tagCorners, tagIds, image, board, charucoCorners, charucoIds);
            if(charucoIds.total()>0){
                corners.add(charucoCorners);
                ids.add(charucoIds);
                corners_count += charucoCorners.rows();
            }
        }

        //try {
        if (corners_count > 4) {
            Log.i(TAG,"Image size: "+imageSize.height+", "+imageSize.width);
            Log.i(TAG,"Marker length: "+markerLength);
            Log.i(TAG,"Square length: "+squareLength);
            Aruco.calibrateCameraCharuco(corners, ids, board, imageSize, cameraMatrix, distCoeffs, rvecs, tvecs);
            distortionData = new CameraDistortionCalibrationData(cameraMatrix, distCoeffs);
            propertyWriter.storeConf("cameraMatrix" + camModule.getCameraCode(), distortionData.getCameraMatrix());
            propertyWriter.storeConf("distCoeffs" + camModule.getCameraCode(),   distortionData.getDistCoeffs());
            propertyWriter.storeConf("cameraMatrix_string" + camModule.getCameraCode(), distortionData.getCameraMatrixMat().dump());
            propertyWriter.storeConf("distCoeffs_string" + camModule.getCameraCode(),   distortionData.getDistCoeffsMat().dump());

            propertyWriter.commitConf();


            Toast.makeText(getApplicationContext(), "Calibration successful!", Toast.LENGTH_SHORT).show();

        }
//        catch (Exception e) {
        else {
            Snackbar.make(getWindow().getDecorView(), R.string.txtErrorCalibrating, Snackbar.LENGTH_LONG).setAction("Wiki", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new
                            Intent(Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.urlCalibrationAppWiki)));
                    startActivity(browserIntent);
                }
            }).show();
//            Toast.makeText(getApplicationContext(), "Error calibrating! Make sure that the board it's clear on all pictures and from different angles.", Toast.LENGTH_SHORT).show();
        }

        capturedList.clear();
        updateImageCounter();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tag_calibration_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.calibSettings) {
            openSettingsPopUp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSettingsPopUp() {
        Intent intent = new Intent(this, TagCalibrationSettingsActivity.class);
        intent.putExtra("squaresX", squaresX);
        intent.putExtra("squaresY", squaresY);
        intent.putExtra("squareLength", squareLength);
        intent.putExtra("markerLength", markerLength);
        startActivityForResult(intent, 1);

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
                camModule.passOCVthings(bridgeBase);
                camModule.signalInit();
                bridgeBase.setVisibility(SurfaceView.VISIBLE);

            }
        });
    }
    @Override
    public void onNewFrame(final Frame frame) {
    }

    @Override
    public void onNewMat(Mat mat) {
        if (mat.cols() > 0 && mat.rows() > 0) {
            if (camModule.getCameraCode() == CAMERA_ID_FRONT)
                Core.flip(mat, mat, 1);

            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);
            Mat newmat = mat.clone();

            if (preview) {
                //newmat = drawArucos(markers, newmat);
                newmat = drawCharuco(mat);

                detected = false;

            }

            if (capturing) {
                capturing = false;

                Mat mat_aux = new Mat();
                //capturedImage = new Mat();
                mat.copyTo(mat_aux);


                ArrayList<Mat> tagCorners = new ArrayList<Mat>();
                Mat tagIds = new Mat();
                Aruco.detectMarkers(mat, Aruco.getPredefinedDictionary(Aruco.DICT_4X4_1000), tagCorners, tagIds);


                final String msg;
                if (tagCorners.size() > 0) {
                    capturedList.add(mat_aux);
                    updateImageCounter();
                    msg = "Image added";
                } else {
                    msg = "Corner not detected, try again";
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            final Frame frame = new Frame(newmat);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(frame.getBitmap());

                }
            });
        }

    }

    private void updateImageCounter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imagesCounter.setText("Images captured: " + capturedList.size());
            }
        });
    }


    private Mat drawArucos(List<Tag> tags, Mat image) {

        for (Tag tag : tags) {
            for (int i = 0; i < 4; i++) {
                Imgproc.line(image, new Point(tag.getCorner(i).x, tag.getCorner(i).y), new Point(tag.getCorner((i + 1) % 4).x, tag.getCorner((i + 1) % 4).y), new Scalar(255, 0, 0));

                Imgproc.circle(image, new Point(tag.getCorner(i).x, tag.getCorner(i).y), 3, new Scalar(0, 255, 0));
            }
        }

        return image;
    }

    private Mat drawCharuco(Mat image) {

        ArrayList<Mat> tagCorners = new ArrayList<Mat>();
        Mat tagIds = new Mat();
        Mat charucoCorners = new Mat();
        Mat charucoIds = new Mat();
        //Todo: add a dropdown to select the type of aruco
        CharucoBoard board = CharucoBoard.create(squaresX, squaresY, squareLength, markerLength, Aruco.getPredefinedDictionary(Aruco.DICT_4X4_1000));
        Aruco.detectMarkers(image, Aruco.getPredefinedDictionary(Aruco.DICT_4X4_1000), tagCorners, tagIds);
        Mat rvecs = new Mat();
        Mat tvecs = new Mat();
        //Aruco.estimatePoseSingleMarkers(markerCorners,14.5f,calibrationData.getCameraMatrixMat(),calibrationData.getDistCoeffsMat(), tvecs, rvecs);
        Aruco.estimatePoseBoard(tagCorners, tagIds, board, distortionData.getCameraMatrixMat(), distortionData.getDistCoeffsMat(), rvecs, tvecs);
        if ((tagCorners.size() > 0) && (tagIds.size().height > 0) && (tagIds.size().width > 0)) {
            Log.w("TAG", "Corners" + tagCorners.size() + " Ids" + tagIds.size());
            Aruco.interpolateCornersCharuco(tagCorners, tagIds, image, board, charucoCorners, charucoIds);
            if (charucoIds.total() > 0)
                Aruco.drawDetectedCornersCharuco(image, charucoCorners);
            //Aruco.drawAxis(image,distortionData.getCameraMatrixMat(), distortionData.getDistCoeffsMat(),rvecs,tvecs,25);
        }
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                squaresX = data.getIntExtra("squaresX", squaresX);
                squaresY = data.getIntExtra("squaresY", squaresY);
                squareLength = data.getFloatExtra("squareLength", squareLength);
                markerLength = data.getFloatExtra("markerLength", markerLength);
                Toast.makeText(getApplicationContext(), "Settings updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDebugFrame(final Frame frame, final String frameId) {
    }

    @Override
    public void onOpenCVStartup() {

        // Set buttons actions

        this.captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturing = true;
            }
        });


        this.changeCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camModule.changeCamera();
                capturedList.clear();
                updateImageCounter();
            }
        });

        this.visualizeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preview = isChecked;

                if (isChecked) {
                    arucoModule.resumeDetection();
                } else {
                    arucoModule.pauseDetection();
                }
            }
        });


        this.calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrateCamera();

            }
        });

        roboboManager.setPowerManagementEnabled(false);

        camModule.changeCamera();
        camModule.setFps(40);

        distortionData = new CameraDistortionCalibrationData(
                propertyWriter.retrieveConf("cameraMatrix" + camModule.getCameraCode(), propertyWriter.retrieveConf("cameraMatrix")),
                propertyWriter.retrieveConf("distCoeffs" + camModule.getCameraCode(), propertyWriter.retrieveConf("distCoeffs")));

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

    @Override
    public void onPause()
    {
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
    public void onResume()
    {
        Log.w("TEST","OnResume");
        super.onResume();
        if (bridgeBase != null)
            bridgeBase.enableView();
            bridgeBase.setCameraPermissionGranted();
    }


}
