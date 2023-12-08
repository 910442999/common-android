package com.yuanquan.common

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AudioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)
        var tv_text1 = findViewById<TextView>(R.id.tv_text1)
        tv_text1.setOnClickListener {

        }
    }
}