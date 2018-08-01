package com.mytechia.robobo.framework.hri.vision.qrTracking;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

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
public class QRInfo {
    private String idString;
    private float xPosition;
    private float yPosition;
    private float distance;

    public QRInfo(String id, float posx, float posy, float distance){
        this.idString = id;
        this.xPosition = posx;
        this.yPosition = posy;
        this.distance = distance;
    }
    public String getIdString() {
        return idString;
    }


    public float getxPosition() {
        return xPosition;
    }


    public float getyPosition() {
        return yPosition;
    }


    public float getDistance() {
        return distance;
    }


    @Override
    public String toString() {
        return "QRInfo{" +
                "idString='" + idString + '\'' +
                ", xPosition=" + xPosition +
                ", yPosition=" + yPosition +
                ", distance=" + distance +
                '}';
    }
}
