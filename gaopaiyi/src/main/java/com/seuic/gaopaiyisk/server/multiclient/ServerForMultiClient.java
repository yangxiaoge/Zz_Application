package com.seuic.gaopaiyisk.server.multiclient;

import com.seuic.gaopaiyisk.MainActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tcp通信服务器
 * https://blog.csdn.net/qq_17007915/article/details/77980633
 * 可以连接多个client,代码可以参考
 * @author Devin Chen
 */
public class ServerForMultiClient  implements MainActivity.SendData2ClientCallback  {
    private static final int PORT = 9999;
    public List<Socket> mClientList = new ArrayList<>();
    private ServerSocket server = null;
    private ExecutorService mExecutors = null; // 线程池对象

    public static void main(String[] args) {
        new ServerForMultiClient().initServer();
    }

    public ServerForMultiClient() {

    }

    public ServerForMultiClient(MainActivity context) {
        context.initCallback(this);
    }


    /**
     * 任务是启动服务器，等待客户端连接
     */
    public void initServer(){
        try {
            server = new ServerSocket(PORT);
            mExecutors = Executors.newCachedThreadPool(); // 创建线程池
            System.out.println("服务器已启动，等待客户端连接...");
            Socket client = null;
            /*
             * 用死循环等待多个客户端的连接，连接一个就启动一个线程进行管理
             */
            while (true) {
                client = server.accept();
                // 把客户端放入集合中
                mClientList.add(client);
                mExecutors.execute(new Service(client)); // 启动一个线程，用以守候从客户端发来的消息
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMsg(String msg) {
        sendMessage(msg);
    }

    class Service implements Runnable {
        private Socket socket;
        private BufferedReader in = null;
        private String message = "";

        public Service(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));// 获得输入流对象
                // 客户端只要一连到服务器，便发送连接成功的信息
                message = "server ip:" + this.socket.getInetAddress() + "client size:" + mClientList.size();
                System.out.println("client message = " + message);
                //this.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            /*//System.out.println("Runnable run");
            try {
                while (true) {
                    if ((message = in.readLine()) != null) {
                        // 当客户端发送的信息为：exit时，关闭连接
                        if (message.equals("exit")) {
                            closeSocket();
                            break;
                        } else {
                            // 接收客户端发过来的信息message，然后转发给客户端。
                            message = socket.getInetAddress() + ":" + message;
                            this.sendMessage(message);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }

        /**
         * 关闭客户端
         *
         * @throws IOException
         */
        public void closeSocket() throws IOException {
            mClientList.remove(socket);
            in.close();
            message = "主机:" + socket.getInetAddress() + "关闭连接\n目前在线:"
                    + mClientList.size();
            socket.close();
            sendMessage(message);
        }

    }

    /**
     * 将接收的消息转发给每一个客户端
     *
     * @param msg
     */
    public void sendMessage(String msg) {
        System.out.println(msg);// 先在控制台输出
        int count = mClientList.size();
        // 遍历客户端集合
        for (int i = 0; i < count; i++) {
            Socket mSocket = mClientList.get(i);
            //客户端是否存活
            /*boolean socketClientAlive = isSocketClientAlive(mSocket, count);
            System.out.println("socketClientAlive = " + socketClientAlive);
            if (!socketClientAlive) {
                i--;
                count--;
                continue;
            }*/

            PrintWriter out = null;
            try {
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(mSocket.getOutputStream())),
                        true);// 创建输出流对象
                out.println(msg);// 转发
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());// 异常输出
            }
        }
    }

    /**
     * 检查当前socket客户端是否还存活
     *
     * @param mSocket socket client
     * @return boolean
     */
    private boolean isSocketClientAlive(Socket mSocket, int count) {
        try {
            mSocket.sendUrgentData(0xFF);
        } catch (Exception ex) {
            ex.printStackTrace();

            mClientList.remove(mSocket);
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
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
            //Log.e(TAG, "获取ip 出错 " + e.toString());
            e.printStackTrace();
        }
        return ip;
    }
}