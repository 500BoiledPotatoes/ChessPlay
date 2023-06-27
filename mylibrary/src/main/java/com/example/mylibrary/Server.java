package com.example.mylibrary;

import android.util.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class Server {
    public static void main(String[] args) {
        new Thread(new startServer()).start();
    }
    private static int port = 6666;
    public static ArrayList<UserThread> socketList = new ArrayList<UserThread>();
    private static class startServer extends Thread{
        public void run(){
            try {
                //绑定服务器要监听的端口
                ServerSocket serverSocket = new ServerSocket(port);
                while(true){
                    //从队列中取出连接请求，使得队列能及时腾出空位，以容纳新的连接请求
                    Socket socket = serverSocket.accept();
                    System.out.println(""+socket);
                    UserThread userThread = new UserThread(socket);
                    socketList.add(userThread);
                    new Thread(userThread).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class UserThread implements Runnable {
        private Socket skt;
        private DataInputStream dis;
        private DataOutputStream dos;

        public DataOutputStream getDos() {
            return dos;
        }

        public UserThread(Socket socket) {
            skt = socket;
        }

        //接收send过来的线程
        @Override
        public void run() {
            try {
                dos = new DataOutputStream(skt.getOutputStream());
                dis = new DataInputStream(skt.getInputStream());
                while (true) {
                    String r_content ;
                    String name ;
                    String time ;
                    String imageId ;
                    time = dis.readUTF();
                    r_content = dis.readUTF();
                    name = dis.readUTF();
                    imageId = dis.readUTF();
                    System.out.println("content = "+r_content);
                    if(time == null){
                        continue;
                    }
                    //发送出去
                    for(UserThread ut : socketList){
                        if(ut.equals(this)){  //是自己的消息就不发出
                            System.out.println("自己的");
                            continue;
                        }
                        try{
                            System.out.println(time);
                            //writeUTF在写入数据流的时候会加上两个字节以表示字节的长度
                            ut.getDos().writeUTF(time);
                            ut.getDos().writeUTF(r_content);
                            ut.getDos().writeUTF(name);
                            ut.getDos().writeUTF(imageId);
                            System.out.println("写出去");
                            //刷新
                            ut.getDos().flush();
                        }catch(Exception e){
                            socketList.remove(ut);
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

