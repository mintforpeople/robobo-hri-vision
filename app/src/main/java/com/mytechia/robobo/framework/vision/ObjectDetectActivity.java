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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.IObjectRecognitionModule;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.IObjectRecognizerListener;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.RecognizedObject;
import com.mytechia.robobo.framework.service.RoboboServiceHelper;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.android.CameraBridgeViewBase.CAMERA_ID_FRONT;

public class ObjectDetectActivity extends AppCompatActivity implements ICameraListener, IObjectRecognizerListener,  GestureDetector.OnGestureListener{
    private static final String TAG="CameraFaceTestActivity";


    private GestureDetectorCompat mDetector;

    private RoboboServiceHelper roboboHelper;
    private RoboboManager roboboManager;


    private ICameraModule camModule;
    private IObjectRecognitionModule objModule;
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
            this.objModule = this.roboboManager.getModuleInstance(IObjectRecognitionModule.class);


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
        objModule.suscribe(this);
        objModule.setConfidence(0.5f);
        camModule.setFps(40);




    }

    @Override
    public void onNewFrame(final Frame frame) {


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
        });

    }

    @Override
    public void onNewMat(Mat mat) {

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
        Log.w(TAG,"Fling!");
        camModule.changeCamera();

        return true;
    }


    @Override
    public void onObjectsRecognized(final List<RecognizedObject> objectList) {

        final TextView tv = this.textView;

        this.objectList = objectList;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String st = "";
                for (RecognizedObject object : objectList){
                    st = st + object.toString() + "\n";
                }
                tv.setText(st);
            }
        });
    }
}
