package com.yuanquan.common

import android.app.Activity
import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.NetworkSpecifier
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import com.yuanquan.common.utils.NetworkUtils
import com.yuanquan.common.utils.permissions.PermissionResultCallback
import com.yuanquan.common.utils.permissions.PermissionUtils
import com.yuanquan.common.widget.SelectableTextHelper


class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var tv_text1 = findViewById<TextView>(R.id.tv_text1)
        tv_text1.setOnClickListener {
//            startActivity(Intent(this, AudioActivity::class.java))
            var intent: Intent = Intent(this, TextLongDownActivity::class.java)
            startActivity(intent)
        }
        var tv_text2 = findViewById<TextView>(R.id.tv_text2)
        tv_text2.setOnClickListener {
            var intent: Intent = Intent(this, TextLongDownActivity2::class.java)
            startActivity(intent)
        }
        var tv_text3 = findViewById<TextView>(R.id.tv_text3)
        tv_text3.setOnClickListener {
            var intent: Intent =
                Intent(this, com.yuanquan.common.selecttext.MainActivity::class.java)
            startActivity(intent)
        }
        var tv_text4 = findViewById<TextView>(R.id.tv_text4)
        tv_text4.setOnClickListener {
            var intent: Intent =
                Intent(this, com.yuanquan.common.selecttext.MainActivity2::class.java)
            startActivity(intent)
        }
        var tv_text5 = findViewById<TextView>(R.id.tv_text5)
        tv_text5.setOnClickListener {
            var intent: Intent = Intent(this, TextLongDownActivity3::class.java)
            startActivity(intent)
        }
    }
}