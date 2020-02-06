package com.mytechia.robobo.framework.hri.vision.cameraStream.websocket;

import java.util.concurrent.LinkedBlockingQueue;

public class ProcessWithQueue extends Thread {
    private LinkedBlockingQueue<byte[]> queue;
    Server server;

    public ProcessWithQueue(LinkedBlockingQueue<byte[]> frameQueue) {
        queue = frameQueue;
        start();
    }

    @Override
    public void run() {
        while (true) {
            while (queue.size() > 0) {
                byte[] frameData = null;
                try {
                    frameData = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                processFrame(frameData);
            }
        }
    }

    private void processFrame(byte[] frameData) {
        synchronized (Server.subscribers) {
            for (int i = 0; i < Server.subscribers.size(); i++) {
                Server.subscribers.get(i).addData(frameData);
            }
        }
    }
}
