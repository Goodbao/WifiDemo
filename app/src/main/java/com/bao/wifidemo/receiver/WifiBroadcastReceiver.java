package com.bao.wifidemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;

import com.bao.wifidemo.R;
import com.bao.wifidemo.utils.WifiControlUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;

/**
 * Created by bao on 2018/3/21.
 * wifi状态广播
 */
public class WifiBroadcastReceiver extends BroadcastReceiver
{
    private WifiControlUtils wifiControlUtils;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        wifiControlUtils = new WifiControlUtils(context);

        //wifi正在改变状态
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction()))
        {
            //获取wifi状态
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLING);
            switch (wifiState)
            {
                case WifiManager.WIFI_STATE_DISABLED:
                    //wifi已经关闭
                    LogUtils.d(context.getString(R.string.wifi_state_disabled));
                    ToastUtils.showShort(context.getString(R.string.wifi_state_disabled));
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    //wifi正在关闭
                    LogUtils.d(context.getString(R.string.wifi_state_disabling));
                    ToastUtils.showShort(context.getString(R.string.wifi_state_disabling));
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    //wifi已经开启
                    LogUtils.d(context.getString(R.string.wifi_state_enabled));
                    ToastUtils.showShort(context.getString(R.string.wifi_state_enabled));
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    //wifi正在开启
                    LogUtils.d(context.getString(R.string.wifi_state_enabling));
                    ToastUtils.showShort(context.getString(R.string.wifi_state_enabling));
                    break;
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction()))
        {
            //网络状态改变
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (NetworkInfo.State.DISCONNECTED.equals(info.getState()))
            {
                //wifi网络连接断开
            } else if (NetworkInfo.State.CONNECTED.equals(info.getState()))
            {
                //获取当前网络，wifi名称
                ToastUtils.showLong(context.getString(R.string.wifi_connected, wifiControlUtils.getWifiInfo().getSSID()));
            }
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction()))
        {
            //wifi密码错误广播
            SupplicantState netNewState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            //错误码
            int netConnectErrorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, WifiManager.ERROR_AUTHENTICATING);
        }
    }
}
