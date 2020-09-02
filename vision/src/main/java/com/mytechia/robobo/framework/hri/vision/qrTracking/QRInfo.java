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
package com.mytechia.robobo.framework.hri.vision.qrTracking;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

import static com.mytechia.robobo.framework.hri.vision.qrTracking.QRUtils.distanceBetweenPoints;
import static com.mytechia.robobo.framework.hri.vision.qrTracking.QRUtils.midPoint;

/**
 * Class that represent the information contained in a qr code
 */
public class QRInfo {

    private String idString;
    private float xPosition;
    private float yPosition;
    private float distance;
    private ResultPoint rp1;
    private ResultPoint rp2;
    private ResultPoint rp3;

    /**
     * Get the first point
     * @return first point
     */
    public ResultPoint getRp1() {
        return rp1;
    }
    /**
     * Get the second point
     * @return second point
     */
    public ResultPoint getRp2() {
        return rp2;
    }
    /**
     * Get the third point
     * @return third point
     */
    public ResultPoint getRp3() {
        return rp3;
    }

    /**
     * Constructor that receives a detection result from zxing
     * @param res Zxing result
     */
    public QRInfo (Result res){
        ResultPoint center = midPoint(res.getResultPoints()[0],res.getResultPoints()[2]);
        this.xPosition = center.getX();
        this.yPosition = center.getY();
        this.distance = distanceBetweenPoints(res.getResultPoints()[1],center);
        this.idString = res.getText();
        this.rp1 = res.getResultPoints()[0];
        this.rp2 = res.getResultPoints()[1];
        this.rp3 = res.getResultPoints()[2];
    }

    /**
     * Returns the encoded string
     * @return String encoded in the QR code
     */
    public String getIdString() {
        return idString;
    }

    /**
     * Gets the position of the code in the x axis
     * @return position
     */
    public float getxPosition() {
        return xPosition;
    }

    /**
     * Gets the position of the code in the y axis
     * @return position
     */
    public float getyPosition() {
        return yPosition;
    }


    /**
     * Gets the distance between the second result point and the center of the code
     * @return distance
     */
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
