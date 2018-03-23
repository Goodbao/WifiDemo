package com.bao.wifidemo.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bao.wifidemo.R;
import com.bao.wifidemo.socket.ServerLastly;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by bao on 2018/3/22.
 * tcp服务器端
 */

public class TcpServerActivity extends AppCompatActivity
{
    @BindView(R.id.et_message)
    EditText etMessage;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.tv_message)
    TextView tvMessage;

    private ServerLastly server;
    private StringBuffer receiveData = new StringBuffer();
    private Handler handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            if (msg.arg1 == ServerLastly.SERVER_ARG)
            {
                receiveData.append("接收到："+(String) msg.obj);
                tvMessage.setText(receiveData);
                receiveData.append("\r\n");
            }
            return false;
        }
    });


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tcp_server_activity);
        ButterKnife.bind(this);

        server = new ServerLastly(handler);
        new Thread(server).start();
    }

    @OnClick(R.id.btn_send)
    public void onViewClicked()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                server.send(etMessage.getText().toString());
            }
        }).start();
        etMessage.setText("");
    }

    @Override
    protected void onDestroy()
    {
        server.close();
        super.onDestroy();
    }
}