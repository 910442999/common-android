package com.yuanquan.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.yuanquan.common.ui.test.ColorPicker2Activity
import com.yuanquan.common.ui.test.ColorPickerActivity

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
        findViewById<TextView>(R.id.tv_text6).setOnClickListener {
            var intent: Intent = Intent(this, ColorPicker2Activity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.tv_text7).setOnClickListener {
            var intent: Intent = Intent(this, ColorPickerActivity::class.java)
            startActivity(intent)
        }
    }
}