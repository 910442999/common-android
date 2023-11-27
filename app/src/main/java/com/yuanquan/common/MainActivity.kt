package com.yuanquan.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var tv_text1 = findViewById<TextView>(R.id.tv_text1)
        tv_text1.setOnClickListener {
            startActivity(Intent(this, AudioActivity::class.java))
        }
        var tv_text2 = findViewById<TextView>(R.id.tv_text2)
        tv_text2.setOnClickListener {
            startActivity(Intent(this, TextViewActivity::class.java))
        }

    }
}