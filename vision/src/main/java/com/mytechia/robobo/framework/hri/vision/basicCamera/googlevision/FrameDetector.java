package com.mytechia.robobo.framework.hri.vision.basicCamera.googlevision;

import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

/*******************************************************************************
 * Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 * Copyright 2016 Luis Llamas <luis.llamas@mytechia.com>
 * <p>
 * This file is part of Robobo Remote Control Module.
 * <p>
 * Robobo Remote Control Module is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Robobo Remote Control Module is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Robobo Remote Control Module.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
public class FrameDetector extends Detector {
    private GoogleVisionCameraModule cameraModule;
    public FrameDetector(GoogleVisionCameraModule cameraModule) {
        this.cameraModule = cameraModule;
        this.setProcessor(new testProcessor());
    }

    @Override

    public SparseArray detect(Frame frame) {
        sendFrame(frame);
        //System.out.println("FRAME");
        return null;
    }

    private void sendFrame(Frame frame){
        cameraModule.receiveFrame(frame);
    }

    class testProcessor implements Processor{

        @Override
        public void release() {

        }

        @Override
        public void receiveDetections(Detections detections) {
            this.release();
        }
    }
}
