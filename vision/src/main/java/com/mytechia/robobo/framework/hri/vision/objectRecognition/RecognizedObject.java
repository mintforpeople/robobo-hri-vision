package com.mytechia.robobo.framework.hri.vision.objectRecognition;

import android.graphics.RectF;

public class RecognizedObject {
    private String label;
    private Float confidence;
    private RectF boundingBox;


    public RecognizedObject(String label, Float confidence, RectF boundingBox) {
        this.label = label;
        this.confidence = confidence;
        this.boundingBox = boundingBox;
    }

    public String getLabel() {
        return label;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    public Float getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "Tag: " + this.label + " Conf: "+(int)(this.confidence*100)+"% x: " + (int)boundingBox.centerX() + " y: " +(int) boundingBox.centerY() ;
    }
}
