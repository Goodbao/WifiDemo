# WifiDemo
Android Wifi控制、TCP、UDP通信，6.0以上适配

[详细文档](https://www.jianshu.com/p/572ac573e4b8)

![wifi连接](https://upload-images.jianshu.io/upload_images/1627327-76fc1f9ea704758a.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/519/format/webp)

![wifi信息](https://upload-images.jianshu.io/upload_images/1627327-be923441db25be08.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/517/format/webp)

对于6.0以上，如果你要连接不是自己创建的配置，只需要在mWifiManager.getConfiguredNetworks()，翻出以前连接过的的Wifi 配置，获取对应的netId,就能重新连接上。

如果以前连接过的 Wifi 密码改了，但是名称没变，你是连不上的，也没权限去修改密码和删除（可能就是为了安全吧），你就要手动去处理这个Wifi 信息了。

APP没有权限删除之前的连接过的 Wifi ，包括APP以前本身创建的 Wifi（先创建了，重装或者更新后，都不算是自己创建了）。

对于从来都没连接过的 Wifi，或者是删除过的 Wifi（相当于没连接过），和以前一样，只要用 SSID （Wifi名）、密码、加密方式创建新的 WifiConfiguration，无密码的就是要 SSID；然后 mWifiManager.enableNetwork 就连上了。

```
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
```




