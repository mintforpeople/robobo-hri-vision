package com.mytechia.robobo.framework.hri.vision.colorMesaurement.opencv;

import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.colorMesaurement.AColorMesaurementModule;

import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by luis on 19/1/17.
 */

public class OpenCVColorMesaurementModule extends AColorMesaurementModule implements ICameraListener {
    private ICameraModule cameraModule;
    private String TAG = "OCVColorMesaurement";
    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        cameraModule = manager.getModuleInstance(ICameraModule.class);
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

    }

    @Override
    public void onNewMat(Mat mat) {

//        Scalar results = Core.mean(mat);
//
//        Log.d(TAG,results.val[0]+"R");
//        Log.d(TAG,results.val[1]+"G");
//        Log.d(TAG,results.val[2]+"B");
//        mat.release();

        int r = 0;
        int g = 0;
        int b = 0;
        long pixels = mat.rows()/4* mat.cols()/4;
        long count =0;

        Mat hsvMat = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(mat, hsvMat, Imgproc.COLOR_RGB2HSV, 3);



        for (int row = 0; row<(hsvMat.rows()); row+=4){
            for (int col = 0; col<(hsvMat.cols()); col+=4) {
                //Log.d(TAG, "Row: "+row+" Col: "+col);
                double[] pixel = hsvMat.get(row,col);




                int hue = (int) Math.round(pixel[0]);
                int saturation = (int) Math.round(pixel[1]);
                int value = (int) Math.round(pixel[2]);

                if ((value > 73) && (saturation > 55)){
                    hue = hue - 8;
                    count = count +1;
                    if (hue < 0) {
                        hue = 171 + Math.abs(hue);
                    }


                    if ((hue > 166) && (hue <= 179)) {
    //                    Log.d(TAG, "RED" + hue);
                        r = r + 1;
                    }

                    if ((hue > 0) && (hue <= 29)) {
    //                    Log.d(TAG, "YELLOW" + hue);
                        count = count -1;
                    }
                    if ((hue > 30) && (hue <= 66)) {
    //                    Log.d(TAG, "GREEN" + hue);
                        g = g + 1;
                    }
                    if ((hue > 67) && (hue <= 96)) {
    //                    Log.d(TAG, "CYAN" + hue);
                        b = b + 1;

                    }
                    if ((hue > 97) && (hue <= 141)) {
    //                    Log.d(TAG, "BLUE" + hue);
                        b = b + 1;

                    }
                    if ((hue > 142) && (hue <= 165)) {
    //                    Log.d(TAG, "MAGENTA" + hue);

                        r = r + 1;
                    }
                }

            }
        }

        int sum = r+g+b;
        //sum = (hsvMat.cols()*hsvMat.rows())/8;
        if(sum!=0) {
            r = Math.round(((float)r / (float)sum) * 100);
            g = Math.round(((float)g / (float)sum) * 100);
            b = Math.round(((float)b / (float)sum) * 100);

            Log.d(TAG, "R: " + r + " G: " + g + " B: " + b+" Covered: "+(float)count/(float)pixels);
            notifyColorMesaured(r, g, b);
        }
        else {
            Log.d(TAG, "R: " + 0 + " G: " + 0 + " B: " + 0);
            notifyColorMesaured(0, 0, 0);

        }
        mat.release();
        hsvMat.release();


    }





}

