package com.mytechia.robobo.framework.hri.vision.cameraStream;

import android.os.AsyncTask;

import java.nio.channels.SocketChannel;

public abstract class AServerThread extends AsyncTask<Void, Void, Void> {
    protected abstract void add_data(byte[] image);

    protected abstract void add_channel(SocketChannel channel);

    protected abstract void close();
}
