package com.mytechia.robobo.framework.hri.vision.util;

import android.support.annotation.NonNull;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class CameraDistortionCalibrationData {

    private Mat cameraMatrix;
    private Mat distCoeffs;
    public int cameraCode;

    //private List<String> rvecs;
    //private List<String> tvecs;
    public CameraDistortionCalibrationData(@NonNull String cameraMatrix,@NonNull String distCoeffs) {//}, List<Mat> rvecs, List<Mat> tvecs){

        this.cameraMatrix = cameraMatrix.isEmpty() ? Mat.eye(3,3, CvType.CV_64F) : GsonConverter.matFromJson(cameraMatrix);
        this.distCoeffs = distCoeffs.isEmpty()? new Mat() : GsonConverter.matFromJson(distCoeffs);
        /*this.rvecs = new ArrayList<>();
        this.tvecs = new ArrayList<>();

        for (int i = 0; i < 3; i++){
            this.rvecs.add(i,GsonConverter.matToJson(rvecs.get(i)));
            this.tvecs.add(i,GsonConverter.matToJson(tvecs.get(i)));
        }*/


    }


    public CameraDistortionCalibrationData(Mat cameraMatrix, Mat distCoeffs) {//, List<String> rvecs, List<String> tvecs){
        this.cameraMatrix = cameraMatrix.clone();
        this.distCoeffs = distCoeffs.clone();
        //this.rvecs = rvecs;
        //this.tvecs = tvecs;

    }



    public Mat getCameraMatrixMat() {
        return cameraMatrix;
    }

    public Mat getDistCoeffsMat() {
        return distCoeffs;
    }

    public String getCameraMatrix() {
        return GsonConverter.matToJson(cameraMatrix);
    }

    public String getDistCoeffs() {
        return GsonConverter.matToJson(distCoeffs);
    }

    /*public List<String> getRvecs() {
        return rvecs;
    }

    public List<String> getTvecs() {
        return tvecs;
    }

    public List<Mat> getRvecsMat() {
        List<Mat> rvecMat = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            rvecMat.add(i,GsonConverter.matFromJson(rvecs.get(i)));
        }
        return rvecMat;
    }

    public List<Mat> getTvecsMat() {
        List<Mat> tvecMat = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            tvecMat.add(i,GsonConverter.matFromJson(tvecs.get(i)));
        }
        return tvecMat;
    }
    */


}
