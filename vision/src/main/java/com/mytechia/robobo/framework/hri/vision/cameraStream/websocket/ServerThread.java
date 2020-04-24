package com.mytechia.robobo.framework.hri.vision.cameraStream.websocket;

import android.os.AsyncTask;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ServerThread is the class that handles the communication of the socket subscribers
 */
public class ServerThread extends AsyncTask<Void, Void, Void> {
    private final int maxQueueLength;
    private volatile SocketChannel channel = null; //The comunication channel
    private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>(); //Queue of images, this is thread-safe

    ServerThread(SocketChannel socketChannel, int queueLength) {
        this.channel = socketChannel;
        this.maxQueueLength=queueLength;
    }

    protected Void doInBackground(Void... urls) {
        try {

            while (channel != null) {

                byte[] temp = queue.take(); //Take waits until it can be done
                ByteBuffer buffer = ByteBuffer.allocate(temp.length + 4).putInt(temp.length);
                buffer.put(temp);
                buffer.rewind();
                while (buffer.hasRemaining())
                    channel.write(buffer);

            }

        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
        return null;
    }

    /**
     * Adds an image to be processed
     *
     * @param image Image to be added
     */
    synchronized void addData(byte[] image) {

        try {
            if (this.queue.size() == maxQueueLength) {
                this.queue.take();
            }
            this.queue.put(image);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    /**
     * Closes the connection with the client
     */
    synchronized void close() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        channel = null;
        Server.subscribers.remove(this);
    }
}

