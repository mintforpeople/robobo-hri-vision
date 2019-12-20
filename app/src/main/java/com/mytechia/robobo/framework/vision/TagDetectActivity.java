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
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.aruco.Tag;
import com.mytechia.robobo.framework.hri.vision.aruco.ITagListener;
import com.mytechia.robobo.framework.hri.vision.aruco.ITagModule;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.IObjectRecognitionModule;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.RecognizedObject;
import com.mytechia.robobo.framework.service.RoboboServiceHelper;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

public class TagDetectActivity extends AppCompatActivity implements ICameraListener, GestureDetector.OnGestureListener, ITagListener {
    private static final String TAG="CameraFaceTestActivity";


    private GestureDetectorCompat mDetector;

    private RoboboServiceHelper roboboHelper;
    private RoboboManager roboboManager;


    private ICameraModule camModule;
    private IObjectRecognitionModule objModule;
    private ITagModule arucoModule;
    private CameraBridgeViewBase bridgeBase;


    private RelativeLayout rellayout = null;
    private TextView textView = null;
    private SurfaceView surfaceView = null;
    private ImageView imageView = null;
    private TextureView textureView = null;
    private Frame actualFrame ;

    private Frame lastFrame;
    private boolean paused = true;
    private long lastDetection = 0;
    private int index = CAMERA_ID_FRONT;

    private List<RecognizedObject> objectList;
    List<Mat> corners;
    List<Tag> markers;
    Mat ids;
    boolean detected = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"TouchEvent");

        return true;

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        objectList = new ArrayList<RecognizedObject>();

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

        //this.surfaceView = (SurfaceView) findViewById(R.id.testSurfaceView);
        this.imageView = (ImageView) findViewById(R.id.testImageView) ;
        this.bridgeBase = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        this.textView = (TextView) findViewById(R.id.textView2);

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
//        ballTrackingModule.configureDetection(true,false, false);


        //camModule.passSurfaceView(surfaceView);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                bridgeBase.setVisibility(SurfaceView.VISIBLE);
                camModule.passOCVthings(bridgeBase);


                camModule.signalInit();


            }
        });
        mDetector = new GestureDetectorCompat(getApplicationContext(),this);
        camModule.suscribe(this);
        camModule.changeCamera();
        arucoModule.suscribe(this);
        camModule.setFps(40);




    }

    @Override
    public void onNewFrame(final Frame frame) {

        /*
        lastFrame = frame;
        Canvas canvas = new Canvas(frame.getBitmap());
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);

        for (RecognizedObject obj: objectList){
            canvas.drawCircle(obj.getBoundingBox().centerX(),obj.getBoundingBox().centerY(), 5, paint);
            canvas.drawText(obj.getLabel(),obj.getBoundingBox().centerX()+7,obj.getBoundingBox().centerY(),paint);
            canvas.drawRect(obj.getBoundingBox(), paint);

        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {



                imageView.setImageBitmap(frame.getBitmap());

            }
        });*/

    }

    @Override
    public void onNewMat(Mat mat) {
        Imgproc.cvtColor(mat,mat,Imgproc.COLOR_BGRA2BGR);
        Mat newmat = mat.clone();

        if (detected){
            //Aruco.drawDetectedMarkers(newmat, corners, ids);
            newmat = drawArucos(markers, newmat);
            //Core.flip(mat,mat, 0);
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


    private  Mat drawArucos(List<Tag> tags, Mat image){

        for (Tag tag : tags){
            for (int i = 0; i < 4; i++){
                Imgproc.line(image, new Point(tag.getCorner(i).x,tag.getCorner(i).y),new Point(tag.getCorner((i+1)%4).x,tag.getCorner((i+1)%4).y),new Scalar(255,0,0));

                Imgproc.circle(image,new Point(tag.getCorner(i).x,tag.getCorner(i).y),3, new Scalar(0,255,0));
            }
        }

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
