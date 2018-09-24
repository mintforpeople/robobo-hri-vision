package com.mytechia.robobo.framework.hri.vision.qrTracking.zxing;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.LogLvl;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.qrTracking.AQRTrackingModule;
import com.mytechia.robobo.framework.hri.vision.qrTracking.QRInfo;
import com.mytechia.robobo.framework.hri.vision.util.FrameCounter;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.core.Mat;

import static com.mytechia.robobo.framework.hri.vision.qrTracking.QRUtils.distanceBetweenPoints;
import static com.mytechia.robobo.framework.hri.vision.qrTracking.QRUtils.midPoint;

/*******************************************************************************
 *
 *   Copyright 2018 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2018 Luis Llamas <luis.llamas@mytechia.com>
 *
 *   This file is part of Robobo Vision Modules.
 *
 *   Robobo Vision Modules is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo Remote Control Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo Vision Modules.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
public class ZXingQRTrackingModule extends AQRTrackingModule implements ICameraListener {

    private ICameraModule cameraModule = null;
    private Reader reader = null;
    private final String TAG = "QRModule";
    private int lostThreshold = 5;
    private int formatErrorCount = 0;
    private int lostCount = 0;
    private QRInfo currentQr = null;
    private boolean processing = false;
    private FrameCounter fps;



    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {

        m = manager;
        try {
            cameraModule = m.getModuleInstance(ICameraModule.class);
            rcmodule = m.getModuleInstance(IRemoteControlModule.class);

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }
        fps = new FrameCounter();
        cameraModule.suscribe(this);
        reader = new QRCodeMultiReader();
    }

    @Override
    public void shutdown() throws InternalErrorException {

    }

    @Override
    public String getModuleInfo() {
        return "QR Tracking Module";
    }

    @Override
    public String getModuleVersion() {
        return "v1";
    }

    @Override
    public void onNewFrame(Frame frame) {
        if (!processing){

            processing = true;
            fps.newFrame();

            if (fps.getElapsedTime() % 10 == 0) {
                Log.v("QR", "FPS = " + fps.getFPS()+ "  "+fps.getElapsedTime() % 10 );
            }
            Bitmap bMap = frame.getBitmap();

            int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
            //copy pixel data from the Bitmap into the 'intArray' array
            bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

            LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(),intArray);

            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                Result res = reader.decode(bitmap);

                QRInfo qr = new QRInfo(res) ;
                //Log.d(TAG,qr.toString());


                if (currentQr == null){
                    notifyQRAppear(qr);
                    currentQr = qr;

                }else if (!currentQr.getIdString().equals(qr.getIdString())){
                    notifyQRDisappear(currentQr);
                    notifyQRAppear(qr);
                    currentQr = qr;


                }


                notifyQR(qr);
                lostCount = 0;
                formatErrorCount = 0;



            } catch (NotFoundException e) {
                this.lostCount = this.lostCount + 1;
            } catch (ChecksumException | FormatException e) {
                this.formatErrorCount = this.formatErrorCount + 1;

            }

            if (((this.lostCount+this.formatErrorCount/2)>this.lostThreshold)&&(this.currentQr!=null)){
                this.notifyQRDisappear(this.currentQr);
                this.currentQr = null;
            }
            processing = false;
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
    public void setLostThreshold(int threshold) {
        this.lostThreshold = threshold;
    }
}
