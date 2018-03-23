package com.bao.wifidemo.activity;

import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bao.wifidemo.R;
import com.bao.wifidemo.receiver.WifiBroadcastReceiver;
import com.bao.wifidemo.utils.WifiControlUtils;
import com.blankj.utilcode.util.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by bao on 2018/3/21.
 * wifi控制Activity
 */
public class WifiControlActivity extends AppCompatActivity
{
    @BindView(R.id.tv_open_wifi)
    TextView tvOpenWifi;
    @BindView(R.id.tv_close_wifi)
    TextView tvCloseWifi;
    @BindView(R.id.tv_scan_wifi)
    TextView tvScanWifi;
    @BindView(R.id.tv_wifi_info)
    TextView tvWifiInfo;
    @BindView(R.id.et_wifi_name)
    EditText etWifiName;
    @BindView(R.id.et_wifi_pwd)
    EditText etWifiPwd;
    @BindView(R.id.tv_connection_wifi)
    TextView tvConnectionWifi;
    @BindView(R.id.tv_disconnection_wifi)
    TextView tvDisconnectionWifi;
    @BindView(R.id.tv_delete_wifi)
    TextView tvDeleteWifi;
    @BindView(R.id.tv_wifi_message)
    TextView tvWifiMessage;

    private WifiBroadcastReceiver wifiBroadcastReceiver;
    private WifiControlUtils wifiControlUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_control_activity);
        ButterKnife.bind(this);

        //动态注册wifi状态广播
        wifiBroadcastReceiver = new WifiBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        registerReceiver(wifiBroadcastReceiver, intentFilter);

        wifiControlUtils = new WifiControlUtils(this);
    }

    @OnClick({R.id.tv_open_wifi, R.id.tv_close_wifi, R.id.tv_scan_wifi
            , R.id.tv_connection_wifi, R.id.tv_disconnection_wifi, R.id.tv_delete_wifi
            , R.id.tv_wifi_message, R.id.tv_wifi_info})
    public void onViewClicked(View view)
    {
        switch (view.getId())
        {
            case R.id.tv_open_wifi:
                wifiControlUtils.openWifi();
                break;
            case R.id.tv_close_wifi:
                wifiControlUtils.closeWifi();
                break;
            case R.id.tv_scan_wifi:
                wifiControlUtils.scanWifi();
                ToastUtils.showShort("扫描到" + wifiControlUtils.getWifiList().size() + "个wifi");
                StringBuilder stringBuilder = new StringBuilder();
                for (ScanResult scanResult : wifiControlUtils.getWifiList())
                {
                    stringBuilder.append(scanResult.SSID);
                    stringBuilder.append(":");
                    stringBuilder.append(scanResult.BSSID);
                    stringBuilder.append("\n");
                }
                tvWifiMessage.setText(stringBuilder.toString());
                break;
            case R.id.tv_wifi_info:
                tvWifiMessage.setText(wifiControlUtils.getWifiInfo().toString());
                break;
            case R.id.tv_connection_wifi:
                wifiControlUtils.addNetWork(etWifiName.getText().toString(), etWifiPwd.getText().toString(), WifiControlUtils.WIFI_CIPHER_WAP);
                break;
            case R.id.tv_disconnection_wifi:
                wifiControlUtils.disconnectWifi(etWifiName.getText().toString());
                break;
            case R.id.tv_delete_wifi:
                if (!wifiControlUtils.removeWifi(etWifiName.getText().toString()))
                {
                    ToastUtils.showShort(R.string.unable_remove);
                }
                break;
            case R.id.tv_wifi_message:
                tvWifiMessage.setText(wifiControlUtils.getWifiInfo().toString());
                break;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //注销广播
        unregisterReceiver(wifiBroadcastReceiver);
    }
}