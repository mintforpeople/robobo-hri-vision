package com.mytechia.robobo.framework.hri.vision.cameraStream.opencv;

import android.os.AsyncTask;

import com.mytechia.robobo.framework.hri.vision.cameraStream.AServerThread;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerThread extends AServerThread {
    private volatile SocketChannel channel = null;
    private LinkedBlockingQueue<byte[]> mQueue = new LinkedBlockingQueue<byte[]>();

    protected Void doInBackground(Void... urls) {
        try {

            while (true) {
                synchronized (mQueue) {
                    if (mQueue.size() > 0) {
                        byte[] temp = mQueue.take();
                        ByteBuffer buffer = ByteBuffer.allocate(temp.length + 4).putInt(temp.length);
                        buffer.put(temp);
                        buffer.rewind();
                        while (buffer.hasRemaining())
                            channel.write(buffer);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Cerrando");
            close();
        }
        return null;
    }

    @Override
    protected synchronized void add_data(byte[] image) {

        try {
            if (this.mQueue.size() == 30) {
                this.mQueue.take();
            }
            this.mQueue.put(image);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void add_channel(SocketChannel channel) {

        this.channel = channel;

    }

    @Override
    protected synchronized void close() {
        Server.sub_video.remove(this);
    }
}

