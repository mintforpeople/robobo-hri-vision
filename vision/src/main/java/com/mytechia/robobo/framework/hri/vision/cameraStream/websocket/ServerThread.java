package com.mytechia.robobo.framework.hri.vision.cameraStream.websocket;

import android.os.AsyncTask;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ServerThread is the class that handles the communication of the socket subscribers
 */
public class ServerThread extends AsyncTask<Void, Void, Void> {
    private volatile SocketChannel channel = null; //The comunication channel
    private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>(); //Queue of images

    protected Void doInBackground(Void... urls) {
        try {

            while (true) {
                synchronized (queue) {
                    if (queue.size() > 0) {
                        byte[] temp = queue.take();
                        ByteBuffer buffer = ByteBuffer.allocate(temp.length + 4).putInt(temp.length);
                        buffer.put(temp);
                        buffer.rewind();
                        while (buffer.hasRemaining())
                            channel.write(buffer);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
        return null;
    }

    /**
     * Adds an image to be processed
     * @param image Image to be added
     */
    public synchronized void addData(byte[] image) {

        try {
            if (this.queue.size() == 30) {
                this.queue.take();
            }
            this.queue.put(image);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Sets the socket channel for the communication
     * @param channel
     */
    protected void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    /**
     * Closes the connection with the client
     */
    protected synchronized void close() {
        Server.subscribers.remove(this);
    }
}

