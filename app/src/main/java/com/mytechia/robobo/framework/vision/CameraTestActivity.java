package com.mytechia.robobo.framework.vision;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mytechia.robobo.framework.vision.R;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;

import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.colorDetection.IColorDetectionModule;
import com.mytechia.robobo.framework.service.RoboboServiceHelper;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class CameraTestActivity extends AppCompatActivity implements ICameraListener{
    private static final String TAG="CameraTestActivity";


    private RoboboServiceHelper roboboHelper;
    private RoboboManager roboboManager;


    private ICameraModule camModule;
    private IColorDetectionModule colorModule;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);




        //this.surfaceView = (SurfaceView) findViewById(R.id.testSurfaceView);
        this.imageView = (ImageView) findViewById(R.id.testImageView) ;
        this.bridgeBase = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);

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
            public void onError(String errorMsg) {

                final String error = errorMsg;


            }

        });

        //start & bind the Robobo service
        Bundle options = new Bundle();
        roboboHelper.bindRoboboService(options);
    }
    private void startRoboboApplication() {

        try {

            this.camModule = this.roboboManager.getModuleInstance(ICameraModule.class);


        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }


        //camModule.passSurfaceView(surfaceView);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                bridgeBase.setVisibility(SurfaceView.VISIBLE);
                camModule.passOCVthings(bridgeBase);


                camModule.signalInit();


            }
        });
        camModule.suscribe(this);



    }

    @Override
    public void onNewFrame(final Frame frame) {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ViewGroup.LayoutParams params =imageView.getLayoutParams();
                params.height = frame.getBitmap().getHeight();
                params.width = frame.getBitmap().getWidth();

                imageView.setLayoutParams(params);
                imageView.setImageBitmap(frame.getBitmap());

            }
        });
    }

    @Override
    public void onNewMat(Mat mat) {

    }
}
