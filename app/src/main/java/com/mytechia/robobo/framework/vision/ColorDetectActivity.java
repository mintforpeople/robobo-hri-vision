package com.mytechia.robobo.framework.vision;

import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

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
import com.mytechia.robobo.framework.hri.vision.colorDetection.IColorDetectionModule;
import com.mytechia.robobo.framework.hri.vision.colorDetection.IColorListener;

import com.mytechia.robobo.framework.service.RoboboServiceHelper;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

/**
 * Created by luis on 10/8/16.
 */
public class ColorDetectActivity extends Activity implements ICameraListener, IColorListener {
    private static final String TAG="MainActivity";


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
        setContentView(R.layout.activity_robobo_custom_main);
        this.textView = (TextView) findViewById(R.id.textView);
        this.rellayout = (RelativeLayout) findViewById(R.id.rellayout);
        this.bridgeBase = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);

        // this.surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        this.imageView = (ImageView) findViewById(R.id.imageView);

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
            this.colorModule = this.roboboManager.getModuleInstance(IColorDetectionModule.class);

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
        colorModule.suscribe(this);
        colorModule.startDetection();

        //camModule.signalInit();



    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //msgModule.sendMessage("TEST","lfllamas93@gmail.com");
        return super.onTouchEvent(event);
    }


    @Override
    protected void onDestroy() {


        super.onDestroy();

        //we unbind and (maybe) stop the Robobo service on exit
        if (roboboHelper != null) {
            roboboHelper.unbindRoboboService();
        }

    }



    @Override
    public void onNewFrame(final Frame frame) {
        lastFrame = frame;
        //TODO CAMBIAR ESTO ROLLO LISTENERS




    }

    @Override
    public void onNewMat(Mat mat) {

    }

    @Override
    public void onDebugFrame(Frame frame, String frameId) {

    }

    @Override
    public void onOpenCVStartup() {

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        paused = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
    }



    @Override
    public void onNewColor(int colorrgb,final int nearest_color) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {



                rellayout.setBackgroundColor(nearest_color);

            }
        });
    }

//    @Override
//    public void onNewColor(final int colorrgb,final int nearest_color) {
//
//
//
//
//
//
//
//
//
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                imageView.setImageBitmap(lastFrame.getBitmap());
//
//                rellayout.setBackgroundColor(nearest_color);
//
//            }
//        });
//
//
//    }
}
