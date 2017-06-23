/*******************************************************************************
 *
 *   Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2016 Luis Llamas <luis.llamas@mytechia.com>
 *
 *   This file is part of Robobo HRI Modules.
 *
 *   Robobo HRI Modules is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo HRI Modules is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo HRI Modules.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.mytechia.robobo.framework.hri.vision.basicCamera.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.WindowManager;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.LogLvl;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ACameraModule;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;

import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.opencv.android.CameraBridgeViewBase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;



/*
 * https://github.com/jayrambhia/AsynCamera
 */

/**
 * Implementation of the camera module using camera2 library
 */
public class AndroidCameraModule extends ACameraModule {

    //region VAR

    private final static String LOGGER="Camera";

    private static final int BUFFER_NUM_IMAGE=15;


    public static final String CAMERA = "camera";

    public static final String IMAGE_COMPRESSED = "image/compressed";

    public static final String CAMERA_INFO = "/camera_info";

    public static final String JPEG = "jpeg";

    private ChannelBufferOutputStream stream;

    private Context context;

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;

    private CameraDevice cameraDevice;

    private ImageReader imageReader;

    private CameraCaptureSession mCaptureSession;

    private int resolution_height = 640;
    private int resolution_width = 480;

    /**
     * prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private Surface jpegCaptureSurface;

    private  String cameraId = "0";

    private int sequence=0;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private CameraCharacteristics characteristics;

    //endregion

    //region IModule methods

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        this.context = manager.getApplicationContext();
        m = manager;

        Properties properties = new Properties();
        AssetManager assetManager = manager.getApplicationContext().getAssets();

        try {
            InputStream inputStream = assetManager.open("vision.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        resolution_height = Integer.parseInt(properties.getProperty("resolution_height"));
        resolution_width = Integer.parseInt(properties.getProperty("resolution_width"));



//        Bundle roboboOptions = roboboFramework.getOptions();
//        roboName=roboboOptions.getString(RoboboRosModule.ROBOBO_NAME, "");

        PackageManager pm = context.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            throw new RuntimeException("The device has no camera");
        }


        this.stream = new ChannelBufferOutputStream(ChannelBuffers.dynamicBuffer(ByteOrder.LITTLE_ENDIAN, 256));

        this.configureCamera();
    }

    @Override
    public void shutdown() throws InternalErrorException {
        closeCamera();
        stopBackgroundThread();
    }

    @Override
    public String getModuleInfo() {
        return null;
    }

    @Override
    public String getModuleVersion() {
        return null;
    }
    //endregion


    //region ConfigureCamera

    private void configureCamera(){

        startBackgroundThread();

        final CameraManager mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            m.log(LogLvl.ERROR, LOGGER, "Permission camera not granted");
            return;
        }

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                m.log(LogLvl.ERROR, LOGGER, "Time out waiting to lock camera opening.");
            }
        } catch (InterruptedException e) {
            m.log(LogLvl.ERROR, LOGGER, "Time out waiting to lock camera opening.");
            return;
        }



        try {

            PackageManager pm = context.getPackageManager();
            if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                cameraId = getFrontFacingCameraId(mCameraManager);
            }else{
                cameraId = getBackFacingCameraId(mCameraManager);
            }

            mCameraManager.openCamera(cameraId, new CameraDeviceStateCallback(mCameraManager), mBackgroundHandler);
        } catch (CameraAccessException ex) {
            m.log(LogLvl.ERROR, LOGGER, "Error open camera"+ ex.getMessage());
        }


    }

    @Override
    public void signalInit() {

    }


    @Override
    public void passSurfaceView(SurfaceView view) {

    }

    @Override
    public void passOCVthings(CameraBridgeViewBase bridgebase) {

    }

    @Override
    public void changeCamera() {
        //TODO Implementarlo aqui
    }

    //endregion

    //region Callbacks and Listeners

    //region CamaraDevice Callback

    private  class CameraDeviceStateCallback extends CameraDevice.StateCallback {


        private CameraManager mCameraManager;

        public CameraDeviceStateCallback(CameraManager mCameraManager) {
            this.mCameraManager = mCameraManager;
        }

        @Override
        public void onOpened(CameraDevice camera) {

            mCameraOpenCloseLock.release();

            AndroidCameraModule.this.cameraDevice= camera;

            characteristics=null;
            try {
                characteristics= mCameraManager.getCameraCharacteristics(cameraId);
            } catch (CameraAccessException ex) {
                m.log(LogLvl.ERROR, LOGGER, "Error camera access exception"+ ex.getMessage());
            }

            StreamConfigurationMap configs= characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

//            Size[] sizes = configs.getOutputSizes(ImageFormat.JPEG);
            Size[] sizes = configs.getOutputSizes(ImageFormat.JPEG);

            Arrays.sort(sizes, new CompareSizesByArea());

            Size preferredSize= new Size(resolution_height, resolution_height);

            int indexSelectedSize=0;

            for (int index=0; index< sizes.length; index++){
                indexSelectedSize=index;
                Size size=sizes[indexSelectedSize];
                if(size.getWidth()> preferredSize.getWidth()){
                    break;
                }
            }

            Size capturedSize=sizes[indexSelectedSize];

            AndroidCameraModule.this.imageReader= ImageReader.newInstance(capturedSize.getWidth(),capturedSize.getHeight(),ImageFormat.JPEG, BUFFER_NUM_IMAGE);

            imageReader.setOnImageAvailableListener(new ImageAvailableListener(), mBackgroundHandler);

            //Cuando creas un surface el tamaño del surface es importante
            //El dispositivo de la camara solo soporta un cierto tipo de tamaño y por lo tanto
            //debe definirse un tamaño valido para cada surface
            jpegCaptureSurface= imageReader.getSurface();

            List<Surface> surfaces= new ArrayList<Surface>();

            //Añadir el preview capture
            surfaces.add(jpegCaptureSurface);

            try {
                cameraDevice.createCaptureSession(surfaces, new CaptureSessionStateCallback(), mBackgroundHandler);
            } catch (CameraAccessException ex) {
                m.log(LogLvl.ERROR, LOGGER, "Error create camera capture session"+ ex.getMessage());
            }


        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            cameraDevice = null;
            m.log(LogLvl.WARNING, LOGGER, "Disconnected camera[id="+camera.getId()+"]");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice=null;
            m.log(LogLvl.ERROR, LOGGER, "Error openning camera[id="+camera.getId()+"], error="+error);
        }

    }

    //endregion

    //region CaptureSessionState Callback

    private class CaptureSessionStateCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(CameraCaptureSession session) {

            AndroidCameraModule.this.mCaptureSession= session;

            CaptureRequest captureRequest=null;

            CaptureRequest.Builder captureRequestBuilder = null;

            try {
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException ex) {
                m.log(LogLvl.ERROR, LOGGER, "Error create capture request"+ ex.getMessage());
                return;
            }

            captureRequestBuilder.addTarget(jpegCaptureSurface);

            Integer mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            int displayOrientation= display.getRotation();

            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, sensorToDeviceRotation(mSensorOrientation, displayOrientation));

            captureRequestBuilder.set(CaptureRequest.JPEG_QUALITY, (byte)75);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range.create(20,30));


            captureRequest = captureRequestBuilder.build();

            try {

                session.setRepeatingRequest(captureRequest, new CameraCaptureSessionCaptureCallback(), mBackgroundHandler);

            } catch (CameraAccessException ex) {
                m.log(LogLvl.ERROR, LOGGER, "Error create capture request"+ ex.getMessage());
                return;
            }

        }


        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            m.log(LogLvl.ERROR, LOGGER, "Error configuration camera capture session");
        }


    }

    //endregion

    //region Timer

    //endregion

    //region CameraCaptureSessionCapture Callback
    private class CameraCaptureSessionCaptureCallback extends CameraCaptureSession.CaptureCallback {

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

    }
    //endregion

    //region ImageAvailable Listener
    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {

        @Override
        public void onImageAvailable(ImageReader reader) {

            android.media.Image mImage = reader.acquireNextImage();



            sendImage(mImage);


        }

    }

    //endregion

    //endregion

    //region CameraID methods

    private String getBackFacingCameraId(CameraManager cManager) throws CameraAccessException {
        for(final String cameraId : cManager.getCameraIdList()){
            CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
            int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
            if(cOrientation == CameraCharacteristics.LENS_FACING_BACK) return cameraId;
        }
        return null;
    }

    private String getFrontFacingCameraId(CameraManager cManager) throws CameraAccessException {

        PackageManager pm = context.getPackageManager();
        for(final String cameraId : cManager.getCameraIdList()){
            CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
            int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
            if(cOrientation == CameraCharacteristics.LENS_FACING_FRONT) return cameraId;
        }
        return null;
    }

    //endregion

    //region BackgroundThread Methods

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackgroundThread");

        mBackgroundThread.setPriority(Thread.MAX_PRIORITY);
        mBackgroundThread.start();

        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

    }


    private void stopBackgroundThread() {

        m.log(LOGGER, "Stopping background thread");

        if(mBackgroundThread==null){
            return;
        }

        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            m.log(LogLvl.WARNING, LOGGER, e.getMessage());
        }


    }

    //endregion

    //region Send Images

    private synchronized void sendImage(android.media.Image mImage) {



        Frame frame = new Frame();



        String frameId = CAMERA;



        int width= mImage.getWidth();
        int height= mImage.getHeight();

        frame.setFrameId(frameId);
        frame.setSeqNum(sequence);
        frame.setHeight(mImage.getHeight());
        frame.setWidth(mImage.getWidth());
//        mImage.getPlanes()[0].getBuffer();

//        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();

        stream.buffer().writeBytes(mImage.getPlanes()[0].getBuffer());//buffer);

        mImage.close();



        frame.setBitmap(Frame.decodeBytes(stream.buffer().array()));

        stream.buffer().clear();


        notifyFrame(frame);

        sequence++;


    }

    //endregion

    //region Auxiliar Methods

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }

    }


    private static int sensorToDeviceRotation(int sensorOrientation, int deviceOrientation) {


        // Get device orientation in degrees
        int angleDeviceOrientation = ORIENTATIONS.get(deviceOrientation);

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation

        return Math.abs(sensorOrientation -angleDeviceOrientation);



    }

    private void closeCamera() {

        m.log(LOGGER, "Closing camera");

        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }

            if(stream!=null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    m.log(LogLvl.WARNING, LOGGER, e.getMessage());
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }


    }
    //endregion

    @Override
    public void debugFrame(Frame frame, String frameId) {
        notifyDebugFrame(frame, frameId);
    }

    @Override
    public void showFrameInView(boolean set) {

    }

    @Override
    public void setFps(int fps) {

    }


}
