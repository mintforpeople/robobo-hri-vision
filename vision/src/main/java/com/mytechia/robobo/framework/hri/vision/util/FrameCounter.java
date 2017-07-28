package com.mytechia.robobo.framework.hri.vision.util;


/**
 * Keeps a count of frames per second processed by a module
 *
 */
public class FrameCounter {

    private long startTime = 0;
    private long frameCount = 0;
    private double elapsedTime = 0;

    public void newFrame() {
        if (frameCount == 0) { //first time or overflow
            startTime = System.currentTimeMillis();
        }

        frameCount++;

        elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;
    }

    public double getFPS() {
        if (elapsedTime > 0) {
            return frameCount / elapsedTime;
        }
        else {
            return 0.0;
        }
    }

    public long getElapsedTime() {
        return (long) elapsedTime;
    }

}
