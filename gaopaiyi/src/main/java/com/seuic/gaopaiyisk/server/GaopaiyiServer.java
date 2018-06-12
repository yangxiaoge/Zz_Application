package com.seuic.gaopaiyisk.server;

import android.util.Log;
import android.widget.Toast;

import com.seuic.gaopaiyisk.MainActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 高拍仪服务端，像客户端持续发送条码信息
 */
public class GaopaiyiServer {
    private static String TAG = GaopaiyiServer.class.getSimpleName();
    private MainActivity activity;
    private ServerSocket serverSocket;
    private String message = "";
    private static final int socketServerPORT = 9999;
    public Socket hostThreadSocket;

    public GaopaiyiServer(MainActivity activity) {
        this.activity = activity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    public int getPort() {
        return socketServerPORT;
    }

    public void onDestroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "serverSocket onDestroy 失败");
                e.printStackTrace();
            }
        }
    }

    private class SocketServerThread extends Thread {

        int count = 0;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(socketServerPORT);

                while (true) {
                    Socket socket = serverSocket.accept();
                    count++;
                    message += "#" + count + " from "
                            + socket.getInetAddress() + ":"
                            + socket.getPort() + "\n";
                    Log.e(TAG, "client ip and port = " + message);
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
//                            activity.msg.setText(message);
                        }
                    });

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                            socket, count);
                    socketServerReplyThread.run();

                }
            } catch (IOException e) {
                Log.e(TAG, "SocketServer 初始化失败");
                e.printStackTrace();
            }
        }

    }

    public PrintStream printStream;

    private class SocketServerReplyThread extends Thread {
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            String msgReply = "Hello from Server, you are #" + cnt;

            try {
                outputStream = hostThreadSocket.getOutputStream();
                // TODO: 2018/6/11 这里可以将  printStream加入集合中，然后可以遍历发送给多个客户端
                printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                //printStream.close();
                showToast("客户端已连接");
                message += "replayed: " + msgReply + "\n";

            } catch (IOException e) {
                showToast("客户端已断开");
                Log.e(TAG, "SocketServerReply 出错 " + e.toString());
                e.printStackTrace();
            }
        }

    }

    private void showToast(final String msg) {
        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
//                        ip += "Server running at : " + inetAddress.getHostAddress();
                        ip += inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            Log.e(TAG, "获取ip 出错 " + e.toString());
            e.printStackTrace();
        }
        return ip;
    }
}