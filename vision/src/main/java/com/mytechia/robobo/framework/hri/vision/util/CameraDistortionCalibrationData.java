package com.mytechia.robobo.framework.hri.vision.util;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class CameraDistortionCalibrationData {

    private String cameraMatrix;
    private String distCoeffs;
    //private List<String> rvecs;
    //private List<String> tvecs;
    public CameraDistortionCalibrationData(Mat cameraMatrix, Mat distCoeffs){//}, List<Mat> rvecs, List<Mat> tvecs){
        this.cameraMatrix = GsonConverter.matToJson(cameraMatrix);
        this.distCoeffs = GsonConverter.matToJson(distCoeffs);
        /*this.rvecs = new ArrayList<>();
        this.tvecs = new ArrayList<>();

        for (int i = 0; i < 3; i++){
            this.rvecs.add(i,GsonConverter.matToJson(rvecs.get(i)));
            this.tvecs.add(i,GsonConverter.matToJson(tvecs.get(i)));
        }*/


    }


    public CameraDistortionCalibrationData(String cameraMatrix, String distCoeffs){//, List<String> rvecs, List<String> tvecs){
        this.cameraMatrix = cameraMatrix;
        this.distCoeffs = distCoeffs;
        //this.rvecs = rvecs;
        //this.tvecs = tvecs;

    }


    public Mat getCameraMatrixMat() {
        return GsonConverter.matFromJson(cameraMatrix);
    }

    public Mat getDistCoeffsMat() {
        return GsonConverter.matFromJson(distCoeffs);
    }

    public String getCameraMatrix() {
        return cameraMatrix;
    }

    public String getDistCoeffs() {
        return distCoeffs;
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
