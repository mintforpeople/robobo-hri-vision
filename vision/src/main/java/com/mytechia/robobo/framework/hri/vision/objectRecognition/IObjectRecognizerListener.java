package com.mytechia.robobo.framework.hri.vision.objectRecognition;

import java.util.List;

public interface IObjectRecognizerListener {

    void onObjectsRecognized(List<RecognizedObject> objectList);
}
