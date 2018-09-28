package com.bao.wifidemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bao.wifidemo.R;
import com.bao.wifidemo.utils.Constants;
import com.blankj.utilcode.util.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by bao on 2018/3/21.
 * Udp通信Activity
 * 测试端口号20001，接收端口号4001，ip：224.0.0.1
 */
public class WifiUdpActivity extends AppCompatActivity {
    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.tv_send_information)
    TextView tvSendInformation;
    @BindView(R.id.tv_receive_information)
    TextView tvReceiveInformation;

    /* 用于 udpReceiveAndTcpSend 的3个变量 */
    private Socket socket = null;
    private MulticastSocket multicastSocket = null;
    private DatagramPacket datagramPacket;

    private TcpReceive tcpReceive;
    private UdpReceiveAndtcpSend udpReceiveAndtcpSend;
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_udp_activity);
        ButterKnife.bind(this);


        tvSendInformation.append("\n\n");
        tvReceiveInformation.append("\n\n");
        /* 开一个线程接收tcp 连接*/
        tcpReceive = new TcpReceive();
        /* 开一个线程 接收udp多播 并 发送tcp 连接*/
        udpReceiveAndtcpSend = new UdpReceiveAndtcpSend();

        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        int KEEP_ALIVE_TIME = 1;
        TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        BlockingQueue blockingQueue = new LinkedBlockingQueue<Runnable>();
        threadPoolExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES
                , NUMBER_OF_CORES * 2
                , KEEP_ALIVE_TIME
                , KEEP_ALIVE_TIME_UNIT
                , blockingQueue);


        threadPoolExecutor.execute(tcpReceive);
        threadPoolExecutor.execute(udpReceiveAndtcpSend);
    }

    @OnClick({R.id.btn_start, R.id.btn_stop})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                /* 新开一个线程 发送 udp 多播 */
                UdpBroadCast udpBroadCast = new UdpBroadCast("hi ~!" + System.getProperty("http.agent"));
                threadPoolExecutor.execute(udpBroadCast);
                break;
            case R.id.btn_stop:
                break;
        }
    }


    /**
     * 发送udp多播
     */
    private class UdpBroadCast extends Thread {
        MulticastSocket sender = null;
        DatagramPacket datagramPacket1 = null;
        InetAddress group = null;

        byte[] data = new byte[1024];

        public UdpBroadCast(String dataString) {
            data = dataString.getBytes();
        }

        @Override
        public void run() {
            try {
                sender = new MulticastSocket();
                //ip
                group = InetAddress.getByName(Constants.INSTANCE.getHOST_ADDRESS());
                //端口号
                datagramPacket1 = new DatagramPacket(data, data.length, group, Constants.INSTANCE.getHOST_PORT());
                sender.send(datagramPacket1);
                sender.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 接收udp多播 并 发送tcp 连接
     */
    private class UdpReceiveAndtcpSend extends Thread {
        @Override
        public void run() {
            byte[] data = new byte[1024];
            try {
                InetAddress groupAddress = InetAddress.getByName(Constants.INSTANCE.getHOST_ADDRESS());
                multicastSocket = new MulticastSocket(Constants.INSTANCE.getHOST_PORT());
                multicastSocket.joinGroup(groupAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    datagramPacket = new DatagramPacket(data, data.length);
                    if (multicastSocket != null)
                        multicastSocket.receive(datagramPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (datagramPacket.getAddress() != null) {
                    final String quest_ip = datagramPacket.getAddress().toString();

                    /* 若udp包的ip地址 是 本机的ip地址的话，丢掉这个包(不处理)*/

                    //String host_ip = getLocalIPAddress();

                    String host_ip = getLocalHostIp();

                    LogUtils.d("host_ip:  --------------------  " + host_ip);
                    LogUtils.d("quest_ip: --------------------  " + quest_ip.substring(1));

                    if (!TextUtils.isEmpty(host_ip) && host_ip.equals(quest_ip.substring(1))) {
                        continue;
                    }

                    final String codeString = new String(data, 0, datagramPacket.getLength());

                    tvReceiveInformation.post(new Runnable() {
                        @Override
                        public void run() {
                            tvReceiveInformation.append("收到来自: \n" + quest_ip.substring(1) + "\n" + "的udp请求\n");
                            tvReceiveInformation.append("请求内容: " + codeString + "\n\n");
                        }
                    });
                    try {
                        final String target_ip = datagramPacket.getAddress().toString().substring(1);
                        tvSendInformation.post(new Runnable() {
                            @Override
                            public void run() {
                                tvSendInformation.append("发送tcp请求到: \n" + target_ip + "\n");
                            }
                        });
                        socket = new Socket(target_ip, Constants.INSTANCE.getPHONE_PORT());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {

                        try {
                            if (socket != null)
                                socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    /**
     * 接收tcp连接
     */
    private class TcpReceive extends Thread {
        ServerSocket serverSocket;
        Socket socket;
        BufferedReader in;
        String source_address;

        @Override
        public void run() {
            while (true) {
                serverSocket = null;
                socket = null;
                in = null;
                try {
                    serverSocket = new ServerSocket(Constants.INSTANCE.getPHONE_PORT());
                    socket = serverSocket.accept();
                    if (socket != null) {
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        sb.append(socket.getInetAddress().getHostAddress());

                        String line = null;
                        while ((line = in.readLine()) != null) {
                            sb.append(line);
                        }

                        source_address = sb.toString().trim();
                        tvReceiveInformation.post(new Runnable() {
                            @Override
                            public void run() {
                                tvReceiveInformation.append("收到来自: " + "\n" + source_address + "\n" + "的tcp请求\n\n");
                            }
                        });
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    try {
                        if (in != null)
                            in.close();
                        if (socket != null)
                            socket.close();
                        if (serverSocket != null)
                            serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String getLocalHostIp() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                // 得到每一个网络接口绑定的所有ip
                NetworkInterface nif = en.nextElement();
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()) {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("feige", "获取本地ip地址失败");
            e.printStackTrace();
        }
        return ipaddress;
    }


    @Override
    protected void onDestroy() {
        //关闭 多播socket multicastSocket
        multicastSocket.close();
        threadPoolExecutor.shutdownNow();
        super.onDestroy();
    }
}