package com.bao.wifidemo.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.bao.wifidemo.R
import com.bao.wifidemo.utils.CheckPermission

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var checkPermission: CheckPermission? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        checkPermission = object : CheckPermission(this@MainActivity) {
            override fun permissionSuccess() {
                //权限申请成功
            }
        }

        checkPermission!!.permission(CheckPermission.REQUEST_CODE_PERMISSION_LOCATION)

        findViewById<TextView>(R.id.tv_wifi_control).setOnClickListener(this@MainActivity)
        findViewById<TextView>(R.id.tv_wifi_tcp).setOnClickListener(this@MainActivity)
        findViewById<TextView>(R.id.tv_wifi_udp).setOnClickListener(this@MainActivity)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tv_wifi_control -> {
                startActivity(WifiControlActivity::class.java)
            }

            R.id.tv_wifi_tcp -> {
                startActivity(WifiTcpActivity::class.java)
            }

            R.id.tv_wifi_udp -> {
                startActivity(WifiUdpActivity::class.java)
            }
        }
    }

    private fun startActivity(activity: Class<*>) {
        startActivity(Intent(this@MainActivity, activity))
    }
}