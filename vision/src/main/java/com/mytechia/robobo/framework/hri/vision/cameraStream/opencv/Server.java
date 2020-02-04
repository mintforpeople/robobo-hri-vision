package com.mytechia.robobo.framework.hri.vision.cameraStream.opencv;

import com.mytechia.robobo.framework.hri.vision.cameraStream.AServer;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class Server extends AServer {

    public void run() {
        int port = 3434;

        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            // Server is running now
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                try {
                    if (socketChannel != null) {
                        synchronized (sub_video) {
                            //socketChannel.socket().setKeepAlive(true);
                            ServerThread serverThread = new ServerThread();
                            serverThread.add_channel(socketChannel);
                            serverThread.execute();
                            sub_video.add(serverThread);

                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}