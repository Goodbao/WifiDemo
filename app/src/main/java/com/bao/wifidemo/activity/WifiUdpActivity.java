package com.bao.wifidemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bao.wifidemo.R;
import com.bao.wifidemo.utils.Constants;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by bao on 2018/3/21.
 * Udp通信Activity
 * 测试端口号20001，接收端口号4001，ip：224.0.0.1
 */
public class WifiUdpActivity extends AppCompatActivity
{
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
    private MulticastSocket ms = null;
    private DatagramPacket dp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_udp_activity);
        ButterKnife.bind(this);


        tvSendInformation.append("\n\n");
        tvReceiveInformation.append("\n\n");
          /* 开一个线程接收tcp 连接*/
        new tcpReceive().start();
         /* 开一个线程 接收udp多播 并 发送tcp 连接*/
        new udpReceiveAndtcpSend().start();
    }

    @OnClick({R.id.btn_start, R.id.btn_stop})
    public void onViewClicked(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_start:
                /* 新开一个线程 发送 udp 多播 */
                new udpBroadCast("hi ~!" + System.getProperty("http.agent")).start();
                break;
            case R.id.btn_stop:
                break;
        }
    }


    /**
     * 发送udp多播
     */
    private class udpBroadCast extends Thread
    {
        MulticastSocket sender = null;
        DatagramPacket dj = null;
        InetAddress group = null;

        byte[] data = new byte[1024];

        public udpBroadCast(String dataString)
        {
            data = dataString.getBytes();
        }

        @Override
        public void run()
        {
            try
            {
                sender = new MulticastSocket();
                //ip
                group = InetAddress.getByName(Constants.INSTANCE.getHOST_ADDRESS());
                //端口号
                dj = new DatagramPacket(data, data.length, group, Constants.INSTANCE.getHOST_PORT());
                sender.send(dj);
                sender.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 接收udp多播 并 发送tcp 连接
     */
    private class udpReceiveAndtcpSend extends Thread
    {
        @Override
        public void run()
        {
            byte[] data = new byte[1024];
            try
            {
                InetAddress groupAddress = InetAddress.getByName(Constants.INSTANCE.getHOST_ADDRESS());
                ms = new MulticastSocket(Constants.INSTANCE.getHOST_PORT());
                ms.joinGroup(groupAddress);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            while (true)
            {
                try
                {
                    dp = new DatagramPacket(data, data.length);
                    if (ms != null)
                        ms.receive(dp);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

                if (dp.getAddress() != null)
                {
                    final String quest_ip = dp.getAddress().toString();

                    /* 若udp包的ip地址 是 本机的ip地址的话，丢掉这个包(不处理)*/

                    //String host_ip = getLocalIPAddress();

                    String host_ip = getLocalHostIp();

                    System.out.println("host_ip:  --------------------  " + host_ip);
                    System.out.println("quest_ip: --------------------  " + quest_ip.substring(1));

                    if ((!host_ip.equals("")) && host_ip.equals(quest_ip.substring(1)))
                    {
                        continue;
                    }

                    final String codeString = new String(data, 0, dp.getLength());

                    tvReceiveInformation.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            tvReceiveInformation.append("收到来自: \n" + quest_ip.substring(1) + "\n" + "的udp请求\n");
                            tvReceiveInformation.append("请求内容: " + codeString + "\n\n");
                        }
                    });
                    try
                    {
                        final String target_ip = dp.getAddress().toString().substring(1);
                        tvSendInformation.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                tvSendInformation.append("发送tcp请求到: \n" + target_ip + "\n");
                            }
                        });
                        socket = new Socket(target_ip, Constants.INSTANCE.getPHONE_PORT());
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    } finally
                    {

                        try
                        {
                            if (socket != null)
                                socket.close();
                        } catch (IOException e)
                        {
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
    private class tcpReceive extends Thread
    {
        ServerSocket serverSocket;
        Socket socket;
        BufferedReader in;
        String source_address;

        @Override
        public void run()
        {
            while (true)
            {
                serverSocket = null;
                socket = null;
                in = null;
                try
                {
                    serverSocket = new ServerSocket(Constants.INSTANCE.getPHONE_PORT());
                    socket = serverSocket.accept();
                    if (socket != null)
                    {
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        sb.append(socket.getInetAddress().getHostAddress());

                        String line = null;
                        while ((line = in.readLine()) != null)
                        {
                            sb.append(line);
                        }

                        source_address = sb.toString().trim();
                        tvReceiveInformation.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                tvReceiveInformation.append("收到来自: " + "\n" + source_address + "\n" + "的tcp请求\n\n");
                            }
                        });
                    }
                } catch (IOException e1)
                {
                    e1.printStackTrace();
                } finally
                {
                    try
                    {
                        if (in != null)
                            in.close();
                        if (socket != null)
                            socket.close();
                        if (serverSocket != null)
                            serverSocket.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String getLocalHostIp()
    {
        String ipaddress = "";
        try
        {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements())
            {
                // 得到每一个网络接口绑定的所有ip
                NetworkInterface nif = en.nextElement();
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements())
                {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress())
                    {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e)
        {
            Log.e("feige", "获取本地ip地址失败");
            e.printStackTrace();
        }
        return ipaddress;
    }

    // 按下返回键时，关闭 多播socket ms
    @Override
    public void onBackPressed()
    {
        ms.close();
        super.onBackPressed();
    }
}