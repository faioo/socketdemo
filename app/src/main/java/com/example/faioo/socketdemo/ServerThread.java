package com.example.faioo.socketdemo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.example.faioo.socketdemo.MainActivity.MSG_REV;
import static com.example.faioo.socketdemo.MainActivity.MSG_SEND;

/**
 * Created by Faioo on 2018/5/31.
 */

public class ServerThread implements Runnable {

    //private ServerSocket ss;
    private int mPort;
    private Handler mHandler;
    private Socket s;
    public Handler revHandler;
    OutputStream out = null;
    BufferedReader in = null;
    ServerSocket ss;

    public ServerThread(int port, Handler handler)
    {
        //this.ss = ss;
        mPort = port;
        mHandler = handler;
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(mPort);
            while (true)
            {
                //阻塞
                s = ss.accept();
                //socketArrayList.add(s);
                //write
                out = s.getOutputStream();
                //os.write("hi\n".getBytes());
                //read
                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                //ServerThread2 serverThread2 = new ServerThread2(s);
                //new Thread(serverThread2).start();
                //String line = br2.readLine();
                //show.setText("来自客户端的数据："+line);
                //br2.close();
                //os.close();
                new Thread(){
                    @Override
                    public void run()
                    {
                        super.run();
                        try {
                            String content = null;
                            while ((content = in.readLine()) != null) {
                                if(content == "exit")
                                {
                                    s.close();
                                    in.close();
                                    out.close();
                                }
                                Log.d("fai",content);
                                Message msg = new Message();
                                msg.what = MSG_REV;
                                msg.obj = content;
                                mHandler.sendMessage(msg);
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }.start();
                Looper.prepare();
                revHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == MSG_SEND) {
                            try {
                                out.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
                                out.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.d("fai","异常1");
                            }
                        }
                    }
                };
                Looper.loop();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

