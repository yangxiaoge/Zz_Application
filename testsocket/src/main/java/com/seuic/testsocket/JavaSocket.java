package com.seuic.testsocket;

import android.support.annotation.NonNull;
import android.util.Log;

import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.SocketResponsePacket;

/**
 * Created by yangjianan on 2018/5/30.
 */
public class JavaSocket {
    private SocketClient socketClient;
    public JavaSocket() {
    }

    public static void main(String args[]) {
        System.out.println("Hello World!");

        new Thread(new Runnable() {
            @Override
            public void run() {
                new JavaSocket().agd();
            }
        }).start();
    }


    private void agd (){
//        socketClient = new SocketClient("192.168.1.114", 8885);
        socketClient = new SocketClient("127.0.0.1", 5209);
        socketClient.registerSocketDelegate(new SocketClient.SocketDelegate() {
            @Override
            public void onConnected(SocketClient client) {
                Log.i("Server", "onConnected:");
                socketClient.send("hello, server !--------------------------->Android");
                socketClient.setHeartBeatMessage("hello, server !--------------------------->Android");
            }

            @Override
            public void onDisconnected(SocketClient client) {
                Log.i("Server", "timeout");
                String error = client.getCharsetName();
                Log.i("Server", "timeoutData:"+error);
            }

            @Override
            public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {
                String responseMsg = responsePacket.getMessage();
                int i = 1;
                Log.i("Server", responseMsg);
            }
        });

        socketClient.setConnectionTimeout(1000 * 15);
        socketClient.setHeartBeatInterval(1000);
        socketClient.setRemoteNoReplyAliveTimeout(1000 * 60);
        socketClient.setCharsetName("UTF-8");
        socketClient.connect();
    }
}
