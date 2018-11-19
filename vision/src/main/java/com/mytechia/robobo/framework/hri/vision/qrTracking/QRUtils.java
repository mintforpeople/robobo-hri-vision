/*******************************************************************************
 *
 *   Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2016 Luis Llamas <luis.llamas@mytechia.com>
 *
 *   This file is part of Robobo Remote Control Module.
 *
 *   Robobo Remote Control Module is free software: you can redistribute it and/or modify
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
 *   along with Robobo Remote Control Module.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package com.mytechia.robobo.framework.hri.vision.qrTracking;

import com.google.zxing.ResultPoint;

/**
 * Utility class to be used with the qr tracking module
 */
public class QRUtils {

    /**
     * Obtain the center point between 3 points
     * @param p1 First point
     * @param p2 Second point
     * @param p3 Third point
     * @return Cente point
     */
    public static ResultPoint centerPoints(ResultPoint p1, ResultPoint p2, ResultPoint p3){

        float yDelta_a = p2.getY() - p1.getY();
        float xDelta_a = p2.getX() - p1.getX();
        float yDelta_b = p3.getY() - p2.getY();
        float xDelta_b = p3.getX() - p2.getX();



        float aSlope = yDelta_a / xDelta_a;
        float bSlope = yDelta_b / xDelta_b;
        float centerX = (aSlope*bSlope*(p1.getY() - p3.getY()) + bSlope*(p1.getX() + p2.getX()) - aSlope*(p2.getX()+p3.getX()) )/(2* (bSlope-aSlope) );
        float centerY = -1 * (centerX - (p1.getX()+p2.getX())/2)/aSlope +  (p1.getY()+p2.getX())/2;


        return new ResultPoint(centerX, centerY);
    }

    /**
     * Obtain the mid point between two points
     * @param p1 First point
     * @param p2 Second point
     * @return Midpoint
     */
    public static ResultPoint midPoint(ResultPoint p1, ResultPoint p2){
        return new ResultPoint((p1.getX()+p2.getX())/2,(p1.getY()+p2.getY())/2);
    }

    /**
     * Get the distance betwueen 2 points
     * @param p1 First point
     * @param p2 Second point
     * @return Distance between the two points
     */
    public static float distanceBetweenPoints(ResultPoint p1, ResultPoint p2){
        return (float) Math.sqrt(Math.pow(p2.getX()-p1.getX(),2)+Math.pow(p2.getY()+p1.getY(),2));
    }
}
