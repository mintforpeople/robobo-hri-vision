package com.mytechia.robobo.framework.hri.vision.cameraStream.websocket;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * This class its the server that creates server threads for each connection requested
 */
public class Server extends Thread {

    static volatile ArrayList<ServerThread> subscribers = new ArrayList<>();
    private final int queueLength;
    private boolean isOpen;


    public Server(int queueLength) {
        this.queueLength=queueLength;
    }


    public void run() {

        int port = 40405; // Port chosen for the streaming service
        isOpen = true;
        try {

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));

            // Server is running now
            while (isOpen) {

                try {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    //socketChannel.socket().setKeepAlive(true);
                    ServerThread serverThread = new ServerThread(socketChannel, queueLength);
                    serverThread.execute();
                    subscribers.add(serverThread);

                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    void close() {

        isOpen = false;

        for (ServerThread t :
                subscribers) {
            t.close();
        }
    }

    void addData(byte[] bytes) {
        for (ServerThread st :
                subscribers) {
            st.addData(bytes);
        }
    }
}