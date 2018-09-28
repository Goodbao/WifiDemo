package com.bao.wifidemo.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bao.wifidemo.R;
import com.bao.wifidemo.socket.ClientLastly;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by bao on 2018/3/22.
 * tcp客户端
 */

public class TcpClientActivity extends AppCompatActivity
{
    @BindView(R.id.et_message)
    EditText etMessage;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.tv_message)
    TextView tvMessage;

    private ClientLastly client;
    private StringBuffer receiveData = new StringBuffer();
    private Handler handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            if (msg.arg1 == ClientLastly.CLIENT_ARG)
            {
                receiveData.append("接收到："+(String) msg.obj);
                //收到数据
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
        setContentView(R.layout.tcp_client_activity);
        ButterKnife.bind(this);

        client = new ClientLastly(handler,getIntent().getStringExtra(WifiTcpActivity.Companion.getHOST_IP()));
        new Thread(client).start();
    }

    @OnClick({R.id.btn_send})
    public void onViewClicked(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_send:
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        client.send(etMessage.getText().toString());
                    }
                }).start();
                etMessage.setText("");
                break;
        }
    }

    @Override
    protected void onDestroy()
    {
        client.close();
        super.onDestroy();
    }
}
