package com.seuic.testsocket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.net.URI;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Main2Activity extends AppCompatActivity {

    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    public void connect(View view) {
        IO.Options opts = new IO.Options();
        opts.reconnection = true;
        opts.forceNew = false;
        opts.query = "naghdaoh";
        mSocket = IO.socket(URI.create("http://192.168.1.117:20006"), opts);

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("connected");
                Log.i("testsocket", "connected");
            }
        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                System.out.println("error");
                Log.d("testsocket", "error");
            }
        });

        mSocket.connect();
    }
}
