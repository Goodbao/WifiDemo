package com.bao.wifidemo.application

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils

/**
 * 尝试一下下kotlin，O(∩_∩)O哈哈~
 */
class BaseApplication : Application() {

    /**
     * 相当于下面这段代码，Kotlin里get,set方法可以不用写，默认就有
     * private static BaseApplication instance;

    public static BaseApplication getInstance()
    {
    return instance;
    }
     */
    companion object {
        var instance: BaseApplication? = null
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
        //超级强大的工具类：https://github.com/Blankj/AndroidUtilCode/blob/master/utilcode/README-CN.md
        Utils.init(this)
        LogUtils.getConfig().setGlobalTag("bao")
    }
}
