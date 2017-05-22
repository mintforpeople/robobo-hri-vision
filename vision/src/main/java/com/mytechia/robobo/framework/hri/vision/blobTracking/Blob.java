package com.mytechia.robobo.framework.hri.vision.blobTracking;

import org.opencv.core.Point;

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
