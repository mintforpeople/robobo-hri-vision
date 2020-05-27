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
package com.mytechia.robobo.framework.hri.vision.util;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class GsonConverter {
    public static String matToJson(Mat mat){

        JsonObject obj = new JsonObject();

        if(mat.isContinuous()){
            int cols = mat.cols();
            int rows = mat.rows();
            int elemSize = (int) mat.elemSize();

//            byte[] data = new byte[cols * rows * elemSize];
//            mat.type();
//            mat.convertTo(mat,CV_32F);
//            mat.type();
//            mat.get(0, 0, data);

            obj.addProperty("rows", mat.rows());
            obj.addProperty("cols", mat.cols());
            obj.addProperty("type", mat.type());
            int type = mat.type();


            String dataString = "ERROR";


            if( type == CvType.CV_32S || type == CvType.CV_32SC2 || type == CvType.CV_32SC3 || type == CvType.CV_16S) {
                int[] data = new int[cols * rows * elemSize];
                mat.get(0, 0, data);
                ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
                IntBuffer intBuffer = byteBuffer.asIntBuffer();
                intBuffer.put(data);
                dataString = Base64.encodeToString(byteBuffer.array(),Base64.DEFAULT);
            }
            else if( type == CvType.CV_32F || type == CvType.CV_32FC2) {
                float[] data = new float[cols * rows * elemSize];
                mat.get(0, 0, data);
                ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
                FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
                floatBuffer.put(data);
                dataString = Base64.encodeToString(byteBuffer.array(),Base64.DEFAULT);
            }
            else if( type == CvType.CV_64F || type == CvType.CV_64FC2) {
                double[] data = new double[cols * rows * elemSize];
                mat.get(0, 0, data);
                ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 8);
                DoubleBuffer doubleBuffer= byteBuffer.asDoubleBuffer();
                doubleBuffer.put(data);
                dataString = Base64.encodeToString(byteBuffer.array(),Base64.DEFAULT);
            }
            else if( type == CvType.CV_8U ) {
                byte[] data = new byte[cols * rows * elemSize];
                mat.get(0, 0, data);
                dataString = Base64.encodeToString(data,Base64.DEFAULT);
            }

            obj.addProperty("data", dataString);

            Gson gson = new Gson();

            return gson.toJson(obj);
        } else {
        }
        return "{}";
    }

    public static Mat matFromJson(String json){

        JsonParser parser = new JsonParser();
        JsonObject JsonObject = parser.parse(json).getAsJsonObject();

        int rows = JsonObject.get("rows").getAsInt();
        int cols = JsonObject.get("cols").getAsInt();
        int type = JsonObject.get("type").getAsInt();

        String dataString = JsonObject.get("data").getAsString();

        Mat mat = new Mat(rows, cols, type);
        if( type == CvType.CV_32S || type == CvType.CV_32SC2 || type == CvType.CV_32SC3 || type == CvType.CV_16S) {
            IntBuffer buffer = ByteBuffer.wrap(Base64.decode(dataString.getBytes(),Base64.DEFAULT)).asIntBuffer();
            int[] data = new int[buffer.remaining()];
            mat.put(0, 0, data);
        }
        else if( type == CvType.CV_32F || type == CvType.CV_32FC2) {
            FloatBuffer buffer = ByteBuffer.wrap(Base64.decode(dataString.getBytes(),Base64.DEFAULT)).asFloatBuffer();
            float[] data = new float[buffer.remaining()];
            mat.put(0, 0, data);
        }
        else if( type == CvType.CV_64F || type == CvType.CV_64FC2) {
            DoubleBuffer doubleBuffer = ByteBuffer.wrap(Base64.decode(dataString.getBytes(), Base64.DEFAULT)).asDoubleBuffer();
            double[] data = new double[doubleBuffer.remaining()];
            doubleBuffer.get(data);
            mat.put(0, 0, data);
        }
        else if( type == CvType.CV_8U ) {
            byte[] data = ByteBuffer.wrap(Base64.decode(dataString.getBytes(),Base64.DEFAULT)).array();
            mat.put(0, 0, data);
        }

        return mat;
    }
}
