package com.mytechia.robobo.framework.hri.vision.cameraStream;

import com.mytechia.robobo.framework.hri.vision.cameraStream.opencv.ServerThread;

import java.util.ArrayList;

public class AServer extends Thread {
    public static volatile ArrayList<AServerThread> sub_video =  new  ArrayList<AServerThread>();
}
