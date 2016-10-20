package com.mytechia.robobo.framework.hri.vision.basicCamera.opencv;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Looper;
import android.renderscript.Script;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.TextureView;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.IModule;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ACameraModule;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;


import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Created by luis on 7/8/16.
 */
public class OpenCVCameraModule extends ACameraModule implements CameraBridgeViewBase.CvCameraViewListener2 {
    //region VAR
    private static final String TAG = "OCVCameraModule";
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;
    private Context context;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    //endregion


    //region IModule methods
    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        context = manager.getApplicationContext();
        Looper.prepare();








    }



    @Override
    public void shutdown() throws InternalErrorException {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public String getModuleInfo() {
        return "OpenCvCameraModule";
    }

    @Override
    public String getModuleVersion() {
        return "v0.1";
    }

    @Override
    public void onCameraViewStarted(int width, int height) {


    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "FRAME! AEmflakmwrlgnaelrbnañerbn");
        Bitmap bmp;
        bmp = Bitmap.createBitmap(inputFrame.rgba().cols(), inputFrame.rgba().rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(inputFrame.rgba(), bmp);
        Frame frame = new Frame();
        frame.setBitmap(bmp);
        notifyFrame(frame);

        return null;
    }

    @Override
    public void signalInit() {

        //mOpenCvCameraView = (CameraBridgeViewBase) new JavaCameraView(context,1);

        //mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(1280,720);



        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, context, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void passSurfaceView(SurfaceView view) {

    }

    @Override
    public void passOCVthings(CameraBridgeViewBase bridgebase) {
        mOpenCvCameraView = bridgebase;
    }
    //endregion
    //region OpenCV Methods


    //endregion

    //region OpenCV things



    //endregion
}
