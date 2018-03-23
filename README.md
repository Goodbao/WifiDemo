# WifiDemo
Android Wifi控制、TCP、UDP通信，6.0以上适配

[详细文档](https://www.jianshu.com/p/572ac573e4b8)

对于6.0以上，如果你要连接不是自己创建的配置，只需要在mWifiManager.getConfiguredNetworks()，翻出以前连接过的的Wifi 配置，获取对应的netId,就能重新连接上。
如果以前连接过的 Wifi 密码改了，但是名称没变，你是连不上的，也没权限去修改密码和删除（可能就是为了安全吧），你就要手动去处理这个Wifi 信息了。
APP没有权限删除之前的连接过的 Wifi ，包括APP以前本身创建的 Wifi（先创建了，重装或者更新后，都不算是自己创建了）。
对于从来都没连接过的 Wifi，或者是删除过的 Wifi（相当于没连接过），和以前一样，只要用 SSID （Wifi名）、密码、加密方式创建新的 WifiConfiguration，无密码的就是要 SSID；然后 mWifiManager.enableNetwork 就连上了。


