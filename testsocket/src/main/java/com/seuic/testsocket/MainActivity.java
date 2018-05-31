package com.seuic.testsocket;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.SocketResponsePacket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private SocketClient socketClient;
    public void socketClient() {
        Socket client = null;
//        String address = "192.168.1.114";
//        int port = 8885;
        String address = "192.168.1.117";
//        int port = 52090;
        int port = 20006;
        //content 作为目标字符串被发送
        String content = "xxxxxxxxxxxxx";
        System.out.println("trysocket");
        try {
            client = new Socket(address, port);
            System.out.println("SocketStart");
            PrintWriter pw = new PrintWriter(client.getOutputStream());
            pw.println(content);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            System.out.println(bufferedReader.readLine());

            pw.flush();
            pw.close();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //socketClient();
            }
        }).start();

    }

    public void send(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                socketClient11();
//                socketClient();

                /*// 客户端请求与本机在 20006 端口建立 TCP 连接
                Socket client = null;
                try {
                    client = new Socket("192.168.1.117", 20006);
                    client.setSoTimeout(10000);
                    // 获取键盘输入
                    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                    // 获取 Socket 的输出流，用来发送数据到服务端
                    PrintStream out = new PrintStream(client.getOutputStream());
                    // 获取 Socket 的输入流，用来接收从服务端发送过来的数据
                    BufferedReader buf =  new BufferedReader(new InputStreamReader(client.getInputStream()));
                    boolean flag = true;
                    while(flag){
                        System.out.print("输入信息：");
                        String str = input.readLine();
                        // 发送数据到服务端
                        out.println(str);
                        if("bye".equals(str)){
                            flag = false;
                        }else{
                            try{
                                // 从服务器端接收数据有个时间限制（系统自设，也可以自己设置），超过了这个时间，便会抛出该异常
                                String echo = buf.readLine();
                                System.out.println(echo);
                                final String sss = echo;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, sss, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }catch(SocketTimeoutException e){
                                System.out.println("Time out, No response");
                            }
                        }
                    }
                    input.close();
                    if(client != null){
                        // 如果构造函数建立起了连接，则关闭套接字，如果没有建立起连接，自然不用关闭
                        client.close(); // 只关闭 socket，其关联的输入输出流也会被关闭
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

            }
        }).start();
//        socketClient11();
    }

    private void socketClient11() {
        //        socketClient = new SocketClient("192.168.1.114", 8885);
        socketClient = new SocketClient("192.168.1.117", 20006);
        socketClient.registerSocketDelegate(new SocketClient.SocketDelegate() {
            @Override
            public void onConnected(SocketClient client) {
                Log.i("Server", "onConnected:");
//                socketClient.send("你好");
                socketClient.setHeartBeatMessage("hello, server !--------------------------->Android");
//                socketClient.sendString("string数据");
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
