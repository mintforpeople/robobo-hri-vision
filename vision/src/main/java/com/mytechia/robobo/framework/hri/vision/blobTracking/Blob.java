/*******************************************************************************
 *
 *   Copyright 2017 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2017 Luis Llamas <luis.llamas@mytechia.com>
 *
 *   This file is part of Robobo HRI Modules.
 *
 *   Robobo HRI Modules is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo HRI Modules is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo HRI Modules.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package com.mytechia.robobo.framework.hri.vision.blobTracking;

import org.opencv.core.Point;


public class Blob {
    private Blobcolor color;
    private int x;
    private int y;
    private int size;
    private boolean isBall;
    private boolean isSquare;

    public Blob(Blobcolor color, Point coords, int size, boolean isBall, boolean isSquare) {
        this.color = color;
        this.isBall = isBall;
        this.isSquare = isSquare;
        this.size = size;
        this.x = (int) coords.x;
        this.y = (int) coords.y;
    }

    public boolean isBall() {
        return isBall;
    }

    public boolean isSquare() {
        return isSquare;
    }

    public int getSize() {
        return size;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Blobcolor getColor() {
        return color;
    }

    public void setSquare(boolean square) {
        isSquare = square;
    }

    public void setBall(boolean ball) {
        isBall = ball;
    }
}
