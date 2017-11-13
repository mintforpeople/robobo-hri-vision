/*******************************************************************************
 *
 *   Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2016 Luis Llamas <luis.llamas@mytechia.com>
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
package com.mytechia.robobo.framework.hri.vision.basicCamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Class representing a frame captured by the camera
 */
public class Frame {
    private int width;
    private int height;
    private String frameId;
    private int seqNum;
    private Bitmap bitmap;

    public Frame(){

    }

    /**
     * New frame from a Mat object
     * @param mat
     */
    public Frame(Mat mat){
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(mat, bmp);

        height = bmp.getHeight();
        width = bmp.getWidth();
        bitmap = bmp;

    }

    /**
     * Returns the sequence number
     * @return sequence number
     */
    public int getSeqNum() {
        return seqNum;
    }

    /**
     * Sets the sequence number
     * @param seqNum sequence number
     */
    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }

    /**
     *Returns the frame identifier
     * @return identifier
     */
    public String getFrameId() {
        return frameId;
    }

    /**
     * Sets the frame identifier
     * @param frameId frame id
     */
    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }


    /**
     * Returns the bitmap represention of the frame
     * @return bitmap
     */
    public Bitmap getBitmap() {
        return bitmap;
    }

    /**
     * Sets the bitmap
     * @param bitmap bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    /**
     * Gets vertical resolution
     * @return resolution on Y axis
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets vertical resolution
     * @param height resolution on Y axis
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Gets the horizontal resolution of the image
     * @return resolution on X axis
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the horizontal resolution of the image
     * @param width resolution on X axis
     */
    public void setWidth(int width) {
        this.width = width;
    }

    public static Bitmap decodeBytes(byte[] data){
        BitmapFactory.Options bitmapFactoryOptions=new BitmapFactory.Options();
        bitmapFactoryOptions.inPreferredConfig=Bitmap.Config.RGB_565;
        bitmapFactoryOptions.inPreferQualityOverSpeed=false;


        return BitmapFactory.decodeByteArray(data, 0, data.length,bitmapFactoryOptions);
    }
}
