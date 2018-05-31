package com.seuic.testsocket.client;

import java.io.PrintWriter;
import java.net.Socket;

public class SocketClienttest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub  

        new Thread(new Runnable() {
            @Override
            public void run() {
                new SocketClienttest().socketClient();
            }
        }).start();
    }

    public void socketClient() {
        Socket client = null;
        String address = "127.0.0.1";
//        String address = "192.168.1.114";
//        int port = 8885;
        int port = 5209;
        //content 作为目标字符串被发送
        String content = "hello server";
        try {
            client = new Socket(address, port);
            System.out.println("SocketStart");
            PrintWriter pw = new PrintWriter(client.getOutputStream());
            pw.println(content);
            pw.flush();
            pw.close();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}  