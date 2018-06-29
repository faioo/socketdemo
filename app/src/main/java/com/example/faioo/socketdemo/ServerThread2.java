package com.example.faioo.socketdemo;

import android.os.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Faioo on 2018/5/31.
 * 没用到他
 */

public class ServerThread2 implements Runnable {
    Socket s;
    OutputStream in = null;
    BufferedReader out =null;
    public Handler sendHandler;
    public ServerThread2(Socket s)
    {
        this.s = s;
        try {
            OutputStream in = s.getOutputStream();
            BufferedReader out = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        //读

    }
}
