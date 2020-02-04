package com.mytechia.robobo.framework.hri.vision.cameraStream;

import java.util.concurrent.LinkedBlockingQueue;

public class ProcessWithQueue extends Thread {
    private static final String TAG = "Queue";
    private LinkedBlockingQueue<byte[]> mQueue;
    AServer server;
    public ProcessWithQueue(LinkedBlockingQueue<byte[]> frameQueue) {
        mQueue = frameQueue;
        start();
    }

    @Override
    public void run() {
        while (true) {

            while(mQueue.size()>0){
                byte[] frameData = null;
                try {
                    frameData = mQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                processFrame(frameData);
            }
        }
    }

    private void processFrame(byte[] frameData) {
        //System.out.println("Procesando Frame");
        synchronized (server.sub_video) {
            for (int i = 0; i < AServer.sub_video.size() ; i ++) {
                AServer.sub_video.get(i).add_data(frameData);
            }
        }
       // Log.i(TAG, "test");
    }
}
