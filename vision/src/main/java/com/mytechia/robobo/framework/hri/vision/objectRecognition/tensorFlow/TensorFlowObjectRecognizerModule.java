package com.mytechia.robobo.framework.hri.vision.objectRecognition.tensorFlow;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.LogLvl;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.AObjectRecognitionModule;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.RecognizedObject;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.tensorFlow.tflite.Classifier;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.tensorFlow.tflite.ImageUtils;
import com.mytechia.robobo.framework.hri.vision.objectRecognition.tensorFlow.tflite.TFLiteObjectDetectionAPIModel;
import com.mytechia.robobo.framework.hri.vision.util.FrameCounter;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TensorFlowObjectRecognizerModule extends AObjectRecognitionModule implements ICameraListener {

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    //private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;

    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;

    private static String TAG = "OBJECTDETECTION";
    private Integer sensorOrientation;

    private Classifier detector;

    protected RoboboManager m;
    private ICameraModule cameraModule = null;

    private Boolean isProcessing = false;

    private Bitmap croppedBitmap = null;
    private Matrix frameToCropTransform;

    private Float minConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
    private Integer maxDetections = 10;
    private Boolean paused = true;

    ExecutorService executor;

//    int totalFrameCount = 0;

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {

        try {


            File detectFile = new File (Environment.getExternalStorageDirectory() + "/properties/detect.tflite");
            File labelFile = new File (Environment.getExternalStorageDirectory() + "/properties/labelmap.txt");

            // Check if user configured model exists
            if (detectFile.exists() && labelFile.exists()) {

                detector =
                        TFLiteObjectDetectionAPIModel.create(
                                TF_OD_API_MODEL_FILE,
                                TF_OD_API_LABELS_FILE,
                                TF_OD_API_INPUT_SIZE,
                                TF_OD_API_IS_QUANTIZED);
            }else { //Fallback to internal default model
                detector =
                        TFLiteObjectDetectionAPIModel.create(
                                manager.getApplicationContext().getAssets(),
                                TF_OD_API_MODEL_FILE,
                                "file:///android_asset/labelmap.txt",
                                TF_OD_API_INPUT_SIZE,
                                TF_OD_API_IS_QUANTIZED);
            }
        } catch (final IOException e) {
            e.printStackTrace();
            manager.log(LogLvl.ERROR, "ObjectDetectionModule","Exception initializing classifier!");

        }
        executor = Executors.newFixedThreadPool(1);

        sensorOrientation = 0;


        m = manager;
        // Load camera and remote control modules
        try {
            cameraModule = m.getModuleInstance(ICameraModule.class);
            rcmodule = m.getModuleInstance(IRemoteControlModule.class);

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }

        rcmodule.registerCommand("START-OBJECT-RECOGNITION", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                resumeDetection();
            }
        });

        rcmodule.registerCommand("STOP-OBJECT-RECOGNITION", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                pauseDetection();

            }
        });
//
//        TimerTask updateFPS = new TimerTask() {
//            public void run() {
//                Log.i("Tensorflow Recognizer", Integer.toString(totalFrameCount));
//                totalFrameCount = 0;
//            }
//        };
//
//        Timer t = new Timer();
//        t.scheduleAtFixedRate(updateFPS, 1000, 1000);


        resumeDetection();
    }

    @Override
    public void shutdown() {
        pauseDetection();
    }

    @Override
    public String getModuleInfo() {
        return "TF-Object detection module";
    }

    @Override
    public String getModuleVersion() {
        return "1.0";
    }


    @Override
    public void onNewFrame(final Frame frame) {


        if (!isProcessing && !paused) {
            //todo:add executor
//            totalFrameCount++;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    int cropSize = TF_OD_API_INPUT_SIZE;

                    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

                    frameToCropTransform =
                            ImageUtils.getTransformationMatrix(
                                    frame.getWidth(), frame.getHeight(),
                                    cropSize, cropSize,
                                    sensorOrientation, MAINTAIN_ASPECT);


                    Matrix cropToFrameTransform = new Matrix();
                    frameToCropTransform.invert(cropToFrameTransform);

                    final Canvas canvas = new Canvas(croppedBitmap);
                    canvas.drawBitmap(frame.getBitmap(), frameToCropTransform, null);

                    isProcessing = true;

                    final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);


                    final List<Classifier.Recognition> mappedRecognitions =
                            new LinkedList<Classifier.Recognition>();

                    final List<RecognizedObject> objectsRecognized =
                            new LinkedList<RecognizedObject>();

                    for (final Classifier.Recognition result : results) {
                        if (objectsRecognized.size() <= maxDetections) {
                            final RectF location = result.getLocation();
                            if (location != null && result.getConfidence() >= minConfidence) {
                                cropToFrameTransform.mapRect(location);
                                ((LinkedList<RecognizedObject>) objectsRecognized).addFirst(new RecognizedObject(Integer.parseInt(result.getId()), result.getTitle(), result.getConfidence(), location));
                                //Log.d(TAG, "Detected: "+result.toString());
                                result.setLocation(location);
                                mappedRecognitions.add(result);
                            }
                        }
                    }

                    //Log.d("RECOGNIZER", mappedRecognitions.toString());
                    if (objectsRecognized.size() > 0) {
                        notifyObjectDetected(objectsRecognized, String.valueOf(frame.getSeqNum()));

                    }
                    isProcessing = false;
                }
            });
        }





    }

    @Override
    public void onNewMat(Mat mat) {

    }

    @Override
    public void onDebugFrame(Frame frame, String frameId) {

    }

    @Override
    public void onOpenCVStartup() {
        imgWidth=cameraModule.getResY();
        imgHeight=cameraModule.getResX();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                pauseDetection();
            }
        });
    }

    @Override
    public void setConfidence(Float confLevel) {
        minConfidence = confLevel % 1.0f;
    }

    @Override
    public void setMaxDetections(Integer maxDetectionNumber) {
        maxDetections = maxDetectionNumber;
    }

    @Override
    public void pauseDetection() {
        Log.d(TAG,"Pause detection");
        paused = true;
        cameraModule.unsuscribe(this);
    }

    @Override
    public void resumeDetection() {
        Log.d(TAG,"Resume detection");

        paused = false;
        cameraModule.suscribe(this);
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }
}
