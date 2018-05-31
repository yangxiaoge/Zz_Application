package com.seuic.testsocket.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * 该类为多线程类，用于服务端 
 */  
public class ServerThread implements Runnable {  
  
    private Socket client = null;
    public ServerThread(Socket client){  
        this.client = client;  
    }  
      
    @Override  
    public void run() {  
        try{  
            // 获取 Socket 的输出流，用来向客户端发送数据  
            PrintStream out = new PrintStream(client.getOutputStream());
            // 获取 Socket 的输入流，用来接收从客户端发送过来的数据  
            BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));
            boolean flag =true;  
            while(flag){  
                // 接收从客户端发送过来的数据  
                String str =  buf.readLine();
                System.out.println("str = "+str);
                if(str == null || "".equals(str)){  
                    flag = false;
                    System.out.println("flag1 = "+flag);
                }else{  
                    if("bye".equals(str)){  
                        flag = false;
                        System.out.println("flag2 = "+flag);
                    }else{  
                        // 将接收到的字符串前面加上 echo，发送到对应的客户端  
                        out.println("echo:" + str);
                        System.out.println("flag3 = "+flag);

                        //测试单次用，add by burce yang 2018年5月31日15:34:07
                        //flag = false;
                    }  
                }  
            }  
            out.close();  
            client.close();  
        }catch(Exception e){  
            e.printStackTrace();  
        }  
    }  
  
}  