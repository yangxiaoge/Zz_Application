package com.yjn.websocket.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class WebSocketMainMethod {

    private static int PORT = 1021;

    public static void main(String[] args) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MWebSocketService socketServer = new MWebSocketService(PORT);
                    socketServer.start();

                    String ip = InetAddress.getLocalHost().getHostAddress();
                    int port = socketServer.getPort();
                    System.out.println(String.format("服务已启动: %s:%d", ip, port));

                    InputStreamReader in = new InputStreamReader(System.in);
                    BufferedReader reader = new BufferedReader(in);

                    while (true) {
                        try {
                            String msg = reader.readLine();
                            socketServer.sendToAll(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}