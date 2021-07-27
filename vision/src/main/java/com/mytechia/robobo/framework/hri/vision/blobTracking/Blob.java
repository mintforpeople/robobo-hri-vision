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

/**
 * Class representing the color blobs
 */
public class Blob {
    private Blobcolor color;
    private int x;
    private int y;
    private int size;
    private boolean isBall;
    private boolean isSquare;
    private long frameTimestamp;
    private int frameSequenceNumber;

    /**
     * Public constructor of the Blob object
     * @param color Color of the blob
     * @param coords Position of the Blob
     * @param size Size in pixels
     * @param isBall Is a ball? flag
     * @param isSquare IS a square? flag
     */
    public Blob(Blobcolor color, Point coords, int size, boolean isBall, boolean isSquare, long frameTimestamp, int frameSequenceNumber) {
        this.color = color;
        this.isBall = isBall;
        this.isSquare = isSquare;
        this.size = size;
        this.x = (int) coords.x;
        this.y = (int) coords.y;
        this.frameTimestamp = frameTimestamp;
        this.frameSequenceNumber = frameSequenceNumber;
    }

    /**
     * Checks if the blob is a ball
     * @return true if it is a ball
     */
    public boolean isBall() {
        return isBall;
    }

    /**
     * Checks if the blob is a square
     * @return true if it is a square
     */
    public boolean isSquare() {
        return isSquare;
    }

    /**
     * Returns the size of the blob
     * @return size of the blob in pixels
     */
    public int getSize() {
        return size;
    }

    /**
     * Position in X axis
     * @return horizontal position in pixels
     */
    public int getX() {
        return x;
    }

    /**
     * Position in Y axis
     * @return vertical position in pixels
     */
    public int getY() {
        return y;
    }

    /**
     * Returnd he color of the blob
     * @return a Blobcolor object
     */
    public Blobcolor getColor() {
        return color;
    }

    /**
     * Sets the object as a square
     * @param square true if its a square
     */
    public void setSquare(boolean square) {
        isSquare = square;
    }

    /**
     * Sets the object as a ball
     * @param ball true if its a ball
     */
    public void setBall(boolean ball) {
        isBall = ball;
    }


    public long getFrameTimestamp() {
        return frameTimestamp;
    }
    public long getFrameSequenceNumber() {
        return frameSequenceNumber;
    }

    public void setFrameTimestamp(long frameTimestamp) {
        this.frameTimestamp = frameTimestamp;
    }
}
