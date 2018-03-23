package com.bao.wifidemo.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by bao on 2018/3/21.
 * wifi控制工具
 */
public class WifiControlUtils
{
    //无密码
    static final public int WIFI_CIPHER_NPW = 0;
    //WEP加密
    static final public int WIFI_CIPHER_WEP = 1;
    //WAP加密
    static final public int WIFI_CIPHER_WAP = 2;

    private WifiManager mWifiManager;
    //能够阻止wifi进入睡眠状态，使wifi一直处于活跃状态
    private WifiManager.WifiLock mWifiLock;
    //扫描出的wifi列表
    private List<ScanResult> wifiList;
    //已连接过的wifi列表
    private List<WifiConfiguration> wifiConfigurationList;


    public WifiControlUtils(Context context)
    {
        //获取wifiManager对象
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 打开wifi
     */
    public void openWifi()
    {
        if (!mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(true);
            scanWifi();
        }
    }

    /**
     * 关闭wifi
     */
    public void closeWifi()
    {
        if (mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 获取wifi连接信息
     **/
    public WifiInfo getWifiInfo()
    {
        if (mWifiManager != null)
        {
            return mWifiManager.getConnectionInfo();
        }
        return null;
    }


    /**
     * 搜索wifi
     */
    public void scanWifi()
    {
        mWifiManager.startScan();
        //得到扫描结果
        wifiList = mWifiManager.getScanResults();
        //得到配置过的网络
        wifiConfigurationList = mWifiManager.getConfiguredNetworks();
    }

    /**
     * 获取连接过的wifi
     */
    public List<WifiConfiguration> getWifiConfigurationList()
    {
        //得到配置过的网络
        wifiConfigurationList = mWifiManager.getConfiguredNetworks();
        return wifiConfigurationList;
    }

    /**
     * 获取扫描wifi的列表
     * 同名字的wifi可以出现多个，比如公司里面的wifi都叫同一个名字，但是由不同的路由器发出来的
     */
    public List<ScanResult> getWifiList()
    {
        //得到扫描结果
        wifiList = mWifiManager.getScanResults();
        return wifiList;
    }

    /**
     * 创建一个WifiLock
     **/
    public void createWifiLock()
    {
        mWifiLock = this.mWifiManager.createWifiLock("testLock");
    }

    /**
     * 锁定WifiLock，当下载大文件时需要锁定
     **/
    public void acquireWifiLock()
    {
        mWifiLock.acquire();
    }

    /**
     * 解锁WifiLock
     **/
    public void releaseWifilock()
    {
        if (mWifiLock.isHeld())
        {
            //判断时候锁定
            mWifiLock.acquire();
        }
    }

    /**
     * 连接指定wifi
     * 6.0以上版本，直接查找时候有连接过，连接过的拿出wifiConfiguration用
     * 不要去创建新的wifiConfiguration,否者失败
     */
    public void addNetWork(String SSID, String password, int Type)
    {
        int netId = -1;
        /*先执行删除wifi操作，1.如果删除的成功说明这个wifi配置是由本APP配置出来的；
                           2.这样可以避免密码错误之后，同名字的wifi配置存在，无法连接；
                           3.wifi直接连接成功过，不删除也能用, netId = getExitsWifiConfig(SSID).networkId;*/
        if (removeWifi(SSID))
        {
            //移除成功，就新建一个
            netId = mWifiManager.addNetwork(createWifiInfo(SSID, password, Type));
        } else
        {
            //删除不成功，要么这个wifi配置以前就存在过，要么是还没连接过的
            if (getExitsWifiConfig(SSID) != null)
            {
                //这个wifi是连接过的，如果这个wifi在连接之后改了密码，那就只能手动去删除了
                netId = getExitsWifiConfig(SSID).networkId;
            } else
            {
                //没连接过的，新建一个wifi配置
                netId = mWifiManager.addNetwork(createWifiInfo(SSID, password, Type));
            }
        }

        //这个方法的第一个参数是需要连接wifi网络的networkId，第二个参数是指连接当前wifi网络是否需要断开其他网络
        //无论是否连接上，都返回true。。。。
        mWifiManager.enableNetwork(netId, true);
    }

    /**
     * 获取配置过的wifiConfiguration
     */
    public WifiConfiguration getExitsWifiConfig(String SSID)
    {
        wifiConfigurationList = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : wifiConfigurationList)
        {
            if (wifiConfiguration.SSID.equals("\"" + SSID + "\""))
            {
                return wifiConfiguration;
            }
        }
        return null;
    }

    /**
     * 移除wifi，因为权限，无法移除的时候，需要手动去翻wifi列表删除
     * 注意：！！！只能移除自己应用创建的wifi。
     * 删除掉app，再安装的，都不算自己应用，具体看removeNetwork源码
     *
     * @param netId wifi的id
     */
    public boolean removeWifi(int netId)
    {
        return mWifiManager.removeNetwork(netId);
    }

    /**
     * 移除wifi
     *
     * @param SSID wifi名
     */
    public boolean removeWifi(String SSID)
    {
        if (getExitsWifiConfig(SSID) != null)
        {
            return removeWifi(getExitsWifiConfig(SSID).networkId);
        } else
        {
            return false;
        }
    }

    /**
     * 断开指定ID的网络
     *
     * @param netId 网络id
     */
    public void disconnectWifi(int netId)
    {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    /**
     * 断开指定SSID的网络
     *
     * @param SSID wifi名
     */
    public void disconnectWifi(String SSID)
    {
        if (getExitsWifiConfig(SSID) != null)
        {
            disconnectWifi(getExitsWifiConfig(SSID).networkId);
        }
    }


    /**
     * 创建一个wifiConfiguration
     *
     * @param SSID     wifi名称
     * @param password wifi密码
     * @param Type     加密类型
     * @return
     */
    public WifiConfiguration createWifiInfo(String SSID, String password, int Type)
    {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        //如果有相同配置的，就先删除
        WifiConfiguration tempConfig = getExitsWifiConfig(SSID);
        if (tempConfig != null)
        {
            mWifiManager.removeNetwork(tempConfig.networkId);
            mWifiManager.saveConfiguration();
        }

        //无密码
        if (Type == WIFI_CIPHER_NPW)
        {
            //config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //config.wepTxKeyIndex = 0;
        }
        //WEP加密
        else if (Type == WIFI_CIPHER_WEP)
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        //WPA加密
        else if (Type == WIFI_CIPHER_WAP) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }
}