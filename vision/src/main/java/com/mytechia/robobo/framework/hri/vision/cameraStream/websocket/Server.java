package com.mytechia.robobo.framework.hri.vision.cameraStream.websocket;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * This class its the server that creates server threads for each connection requested
 */
public class Server extends Thread {

    public static volatile ArrayList<ServerThread> subscribers = new ArrayList<>();


    public void run() {

        int port = 3434; // Port chosen for the streaming service

        try {

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));

            // Server is running now
            while (true) {

                SocketChannel socketChannel = serverSocketChannel.accept();
                try {
                    if (socketChannel != null) {
                        synchronized (subscribers) {
                            //socketChannel.socket().setKeepAlive(true);
                            ServerThread serverThread = new ServerThread();
                            serverThread.setChannel(socketChannel);
                            serverThread.execute();
                            subscribers.add(serverThread);

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