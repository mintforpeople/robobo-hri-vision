package com.mytechia.robobo.framework.vision;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mytechia.robobo.framework.hri.vision.blobTracking.IBlobTrackingModule;

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

    private IBlobTrackingModule blobModule;

    private Frame lastFrame;
    private boolean paused = true;
    private long lastDetection = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);

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
        //this.surfaceView.setVisibility(View.INVISIBLE);
        //this.imageView.setVisibility(View.INVISIBLE);
        //this.textureView.setVisibility(View.VISIBLE);




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
            this.blobModule = this.roboboManager.getModuleInstance(IBlobTrackingModule.class);

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }

        camModule.suscribe(this);

        //camModule.passSurfaceView(surfaceView);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                bridgeBase.setVisibility(SurfaceView.VISIBLE);
                camModule.passOCVthings(bridgeBase);
                camModule.signalInit();





            }
        });




    }

    @Override
    public void onNewFrame(final Frame frame) {

        Log.d(TAG, "Frame2");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ViewGroup.LayoutParams params =imageView.getLayoutParams();
                params.height = frame.getHeight();
                params.width = frame.getWidth();

                imageView.setLayoutParams(params);
                imageView.setImageBitmap(frame.getBitmap());

            }
        });
    }

    @Override
    public void onNewMat(Mat mat) {

    }

    @Override
    public void onDebugFrame(Frame frame, String frameId) {

    }

    @Override
    public void onOpenCVStartup() {
        blobModule.configureDetection(true,false,false,false);
    }
}
