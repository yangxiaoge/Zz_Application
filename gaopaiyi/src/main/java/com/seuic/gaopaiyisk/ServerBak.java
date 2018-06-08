package com.seuic.gaopaiyisk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Created by yangjianan on 2018/6/7.
 */
public class ServerBak {

    private static boolean mIsServiceDestroyed = false;
    private static String[] mDefinedMessage = new String[]{
            "你好啊，哈哈", "我帅吗", "今天南京天气怎样", "听说爱笑的人运气不会太差哦！"
    };
    public static void main(String[] args) {
        new Thread(new TcpServer()).start();
    }

    private static class TcpServer implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8688);
                System.out.println("启动服务成功");
            } catch (IOException e) {
                System.err.println("启动服务失败，port:8688");
                e.printStackTrace();
                return;
            }

            while (!mIsServiceDestroyed) {
                try {
                    //接收客户端请求
                    final Socket client = serverSocket.accept();
                    System.out.println("accept");
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void responseClient(Socket client) throws IOException {
        //用于接收客户端信息
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //用于向客户端发送信息
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
        out.print("欢迎来到聊天室！");

        while (!mIsServiceDestroyed) {
            System.out.println("msg from client: in =   " + (in==null));
            System.out.println("msg from client: in.readLine()  " + (in.readLine()==null));
            String str = in.readLine();
            System.out.println("msg from client: " + str);
            if (str == null) {
                //客户端断开连接
                break;
            }
            //服务器随机发送文字给客户端
            int  i = new Random().nextInt(mDefinedMessage.length);
            String msg = mDefinedMessage[i];
            out.println(msg);
            System.out.println("send："+msg);
        }

        System.out.println("客户端退出");
        //关闭流
        out.close();
        in.close();
        client.close();
    }
}
