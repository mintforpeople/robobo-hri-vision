package com.mytechia.robobo.framework.hri.vision.objectRecognition.tensorFlow;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
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
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.core.Mat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TensorFlowObjectRecognizerModule extends AObjectRecognitionModule implements ICameraListener {

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    //private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;

    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;


    private Integer sensorOrientation;

    private Classifier detector;

    protected RoboboManager m;
    private ICameraModule cameraModule = null;

    private Boolean isProcessing = false;

    private Bitmap croppedBitmap = null;
    private Matrix frameToCropTransform;

    private Float minConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
    private Integer maxDetections = 10;


    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {

        try {


            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
            manager.log(LogLvl.ERROR, "ObjectDetectionModule","Exception initializing classifier!");

        }
        sensorOrientation = 0;


        m = manager;
        // LOad camera and remote controlo modules
        try {
            cameraModule = m.getModuleInstance(ICameraModule.class);

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }
        cameraModule.suscribe(this);
    }

    @Override
    public void shutdown() throws InternalErrorException {

    }

    @Override
    public String getModuleInfo() {
        return null;
    }

    @Override
    public String getModuleVersion() {
        return null;
    }


    @Override
    public void onNewFrame(Frame frame) {

        if (!isProcessing) {
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
                        ((LinkedList<RecognizedObject>) objectsRecognized).addFirst(new RecognizedObject(result.getTitle(), result.getConfidence(), location));

                        result.setLocation(location);
                        mappedRecognitions.add(result);
                    }
                }
            }

            //Log.d("RECOGNIZER", mappedRecognitions.toString());
            if (objectsRecognized.size() > 0){
                notifyObjectDetected(objectsRecognized);

            }
            isProcessing = false;
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

    }

    @Override
    public void setConfidence(Float confLevel) {
        minConfidence = confLevel % 1.0f;
    }

    @Override
    public void setMaxDetections(Integer maxDetectionNumber) {
        maxDetections = maxDetectionNumber;
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }
}
