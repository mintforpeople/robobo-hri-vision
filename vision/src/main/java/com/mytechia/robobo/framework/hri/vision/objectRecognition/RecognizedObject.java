package com.mytechia.robobo.framework.hri.vision.objectRecognition;

import android.graphics.RectF;

public class RecognizedObject {
    private String tag;
    private Float confidence;
    private RectF boundingBox;


    public RecognizedObject(String tag,Float confidence, RectF boundingBox) {
        this.tag = tag;
        this.confidence = confidence;
        this.boundingBox = boundingBox;
    }

    public String getTag() {
        return tag;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    public Float getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "Tag: " + this.tag + " Conf: "+(int)(this.confidence*100)+"% x: " + (int)boundingBox.centerX() + " y: " +(int) boundingBox.centerY() ;
    }
}
