package com.yuanquan.common.ui.test

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yuanquan.common.R
import com.yuanquan.common.widget.color_picker.ColorSelectView

class ColorPickerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_picker)
        val tvTest: TextView = findViewById(R.id.tv_text)
        val view: View = findViewById(R.id.view)
        var color_picker = findViewById<ColorSelectView>(R.id.color_picker)
        color_picker.colorChanged = {
            tvTest.setTextColor(it)
            view.setBackgroundColor(it)
        }
        color_picker.colorClicked = { color ->
            tvTest.setTextColor(Color.parseColor(color))
            view.setBackgroundColor(Color.parseColor(color))
        }
    }
}