package com.mytechia.robobo.framework.vision;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.mytechia.robobo.framework.hri.vision.colorDetection.IColorDetectionModule;
import com.mytechia.robobo.framework.hri.vision.faceDetection.IFaceDetectionModule;
import com.mytechia.robobo.framework.hri.vision.faceDetection.IFaceListener;
import com.mytechia.robobo.framework.service.RoboboServiceHelper;

import org.opencv.android.CameraBridgeViewBase;

public class CameraFaceTestActivity extends AppCompatActivity implements ICameraListener, IFaceListener{
    private static final String TAG="CameraFaceTestActivity";


    private RoboboServiceHelper roboboHelper;
    private RoboboManager roboboManager;


    private ICameraModule camModule;
    private IFaceDetectionModule faceModule;
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
            this.faceModule = this.roboboManager.getModuleInstance(IFaceDetectionModule.class);


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
        //faceModule.suscribe(this);




    }

    @Override
    public void onNewFrame(final Frame frame) {


        lastFrame = frame;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG,"Frame!!!!!!!");

                imageView.setImageBitmap(frame.getBitmap());

            }
        });

    }

    @Override
    public void onFaceDetected(final PointF faceCoords, float eyesDistance) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                Canvas c = new Canvas(lastFrame.getBitmap().copy(Bitmap.Config.ARGB_8888,true));
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                c.drawCircle(faceCoords.x,faceCoords.y,50,paint);
                imageView.draw(c);

            }
        });
    }
}
