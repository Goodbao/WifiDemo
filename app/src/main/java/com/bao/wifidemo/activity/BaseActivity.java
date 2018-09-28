package com.bao.wifidemo.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;

import com.bao.wifidemo.utils.Constants;
import com.bao.wifidemo.utils.WifiControlUtils;
import com.blankj.utilcode.util.AppUtils;

import butterknife.ButterKnife;

/**
 * Created by bao on 2018/3/26.
 */
public abstract class BaseActivity extends AppCompatActivity {


    private WifiControlUtils wifiControlUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();
        ButterKnife.bind(this);
        init();
        loadData();


        wifiControlUtils = new WifiControlUtils(this);
    }


    public abstract void setContentView();

    /**
     * 初始化工作
     */
    public abstract void init();

    /**
     * 加载数据
     */
    public abstract void loadData();

    @Override
    protected void onResume() {
        super.onResume();

        if (AppUtils.isAppForeground()) {
            //连接指定wifi
            wifiControlUtils.addNetWork(Constants.INSTANCE.getWIFI_NAME(), Constants.INSTANCE.getWIFI_PWD(), WifiControlUtils.WIFI_CIPHER_WAP);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!AppUtils.isAppForeground()) {
            //移除指定wifi
            wifiControlUtils.removeWifi(Constants.INSTANCE.getWIFI_NAME());
        }
    }


    /**
     * 隐藏软件盘
     */
    public void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * 显示软键盘
     */
    public void showInputMethod() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInputFromInputMethod(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * 防止快速点击
     */
    private boolean fastClick() {
        long lastClick = 0;
        if (System.currentTimeMillis() - lastClick <= 1000) {
            return false;
        }
        lastClick = System.currentTimeMillis();
        return true;
    }

    /**
     * 跳转activity,不带参数
     *
     * @param clz 跳转的activity
     */
    public void startActivity(Class<?> clz) {
        startActivity(new Intent(this, clz));
    }

    /**
     * 跳转activity，带参数
     *
     * @param clz    跳转的activity
     * @param bundle 传递的参数
     */
    public void startActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }


    /**
     * 字体大小不跟系统设置
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {
            //非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();
            //设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }


    @Override
    protected void onDestroy() {
        ButterKnife.bind(this).unbind();
        super.onDestroy();
    }
}