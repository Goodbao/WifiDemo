package com.bao.wifidemo.socket;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.bao.wifidemo.utils.Constants;
import com.blankj.utilcode.util.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 通过socket实现
 *
 * @author Administrator
 */
public class ClientLastly implements Runnable {
    private static final String TAG = "tcp_client";
    public static final int CLIENT_ARG = 0x12;
    //超时时间，如果60S没通信，就会断开
    private int timeout = 60000;
    private Socket clientSocket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    Handler handler;
    private String server_ip;

    public ClientLastly(Handler handler, String server_ip) {
        this.handler = handler;
        this.server_ip = server_ip;
//        try {
//            //连接服务器
//            clientSocket=new Socket("localhost", 8888);
//            Log.i(TAG, "Client=======连接服务器成功=========");
//            clientSocket.setSoTimeout(timeout);
//            printWriter=new PrintWriter(clientSocket.getOutputStream());
//            bufferedReader=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    //发数据
    public void send(String data) {
        LogUtils.dTag(TAG, "客户端发送：" + data);
        if (printWriter != null) {
            printWriter.println(data);
            printWriter.flush();
        }
    }


    //接收据
    @Override
    public void run() {
        try {
            //连接服务器
            clientSocket = new Socket(server_ip, Constants.INSTANCE.getHOST_PORT());
            LogUtils.dTag(TAG, "=======连接服务器成功=========");
            clientSocket.setSoTimeout(timeout);
            printWriter = new PrintWriter(clientSocket.getOutputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            if (bufferedReader != null && !TextUtils.isEmpty(bufferedReader.readLine())) {
                String result = "";
                while ((result = bufferedReader.readLine()) != null) {
                    LogUtils.dTag(TAG, "客户端接到的数据为：" + result);
                    //将数据带回acitvity显示
                    Message msg = handler.obtainMessage();
                    msg.arg1 = CLIENT_ARG;
                    msg.obj = result;
                    handler.sendMessage(msg);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (printWriter != null) {
                printWriter.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}