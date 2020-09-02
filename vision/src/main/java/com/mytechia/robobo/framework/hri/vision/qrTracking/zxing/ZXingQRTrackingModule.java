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
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.core.Mat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mytechia.robobo.framework.hri.vision.qrTracking.QRUtils.distanceBetweenPoints;
import static com.mytechia.robobo.framework.hri.vision.qrTracking.QRUtils.midPoint;

/**
 * Implementation of the QRTrackingModule using ZXing library
 */
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
    ExecutorService executor;


    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {

        m = manager;
        // Create framecounter
        fps = new FrameCounter();
        // Create Qr code reader
        reader = new QRCodeMultiReader();
        // Create thread executor to avoid locking OnNewFrame thread
        executor = Executors.newFixedThreadPool(1);

        // LOad camera and remote controlo modules
        try {
            cameraModule = m.getModuleInstance(ICameraModule.class);
            rcmodule = m.getModuleInstance(IRemoteControlModule.class);

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }
        rcmodule.registerCommand("START-QR-TRACKING", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                startDetection();
            }
        });

        rcmodule.registerCommand("STOP-QR-TRACKING", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                stopDetection();

            }
        });
        startDetection();

    }

    private void stopDetection() {
        cameraModule.unsuscribe(this);
    }

    private void startDetection() {
        cameraModule.suscribe(this);
    }

    @Override
    public void shutdown() {
        stopDetection();
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
    public void onNewFrame(final Frame frame) {
        // If it not already processing a frame
        if (!processing) {
            // Execute on thread
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    processing = true;
                    fps.newFrame();

                    if (fps.getElapsedTime() % 10 == 0) {
                        Log.v("QR", "FPS = " + fps.getFPS() + "  " + fps.getElapsedTime() % 10);
                    }
                    Bitmap bMap = frame.getBitmap();

                    int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
                    //copy pixel data from the Bitmap into the 'intArray' array
                    bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

                    LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);

                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    try {
                        // Decode image to find QR codes
                        Result res = reader.decode(bitmap);
                        // Create a QRInfo object with the result
                        QRInfo qr = new QRInfo(res);
                        //Log.d(TAG,qr.toString());


                        if (currentQr == null) {
                            notifyQRAppear(qr);
                            currentQr = qr;

                        } else if (!currentQr.getIdString().equals(qr.getIdString())) {
                            notifyQRDisappear(currentQr);
                            notifyQRAppear(qr);
                            currentQr = qr;


                        }


                        notifyQR(qr, frame.getFrameId());
                        lostCount = 0;
                        formatErrorCount = 0;


                    } catch (NotFoundException e) {
                        ZXingQRTrackingModule.this.lostCount = ZXingQRTrackingModule.this.lostCount + 1;
                    } catch (ChecksumException | FormatException e) {
                        ZXingQRTrackingModule.this.formatErrorCount = ZXingQRTrackingModule.this.formatErrorCount + 1;

                    }

                    if (((ZXingQRTrackingModule.this.lostCount + ZXingQRTrackingModule.this.formatErrorCount / 2) > ZXingQRTrackingModule.this.lostThreshold) && (ZXingQRTrackingModule.this.currentQr != null)) {
                        ZXingQRTrackingModule.this.notifyQRDisappear(ZXingQRTrackingModule.this.currentQr);
                        ZXingQRTrackingModule.this.currentQr = null;
                    }
                    processing = false;
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

    }

    @Override
    public void setLostThreshold(int threshold) {
        this.lostThreshold = threshold;
    }
}
