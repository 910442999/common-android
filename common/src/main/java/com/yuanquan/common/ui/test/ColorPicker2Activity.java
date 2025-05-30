package com.yuanquan.common.ui.test;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yuanquan.common.R;
import com.yuanquan.common.widget.color_picker.ColorPickerView;


public class ColorPicker2Activity extends AppCompatActivity {

    private int or = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker2);

        ColorPickerView picker = (ColorPickerView) findViewById(R.id.colorPickerView);
        picker.setIndicatorColor(Color.GREEN);
        picker.setOrientation(ColorPickerView.Orientation.HORIZONTAL);
        picker.setColors(Color.DKGRAY,Color.RED,Color.WHITE);
        picker.setOnColorPickerChangeListener(new ColorPickerView.OnColorPickerChangeListener() {
            @Override
            public void onColorChanged(ColorPickerView picker, int color) {
                // TODO
            }

            @Override
            public void onStartTrackingTouch(ColorPickerView picker) {
                // TODO

            }

            @Override
            public void onStopTrackingTouch(ColorPickerView picker) {
                // TODO

            }
        });


        final View show = findViewById(R.id.show);
        final TextView argb = (TextView) findViewById(R.id.textView);
        final ColorPickerView center = (ColorPickerView) findViewById(R.id.colorPickerView);
        final ColorPickerView top = (ColorPickerView) findViewById(R.id.picker1);
        final ColorPickerView belowTop = (ColorPickerView) findViewById(R.id.colorPickerView3);

        final ColorPickerView right = (ColorPickerView) findViewById(R.id.colorPickerView2);
        final ColorPickerView left = (ColorPickerView) findViewById(R.id.picker);

        show.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int i = or % 2;
                or++;

                String ori = "";
                if (i == 0) {
                    belowTop.setOrientation(ColorPickerView.Orientation.HORIZONTAL);
                    ori = ColorPickerView.Orientation.HORIZONTAL.toString();
                } else {
                    belowTop.setOrientation(ColorPickerView.Orientation.VERTICAL);
                    ori = ColorPickerView.Orientation.VERTICAL.toString();
                }

                Toast.makeText(ColorPicker2Activity.this, ori, Toast.LENGTH_SHORT).show();

            }
        });
        ColorPickerView.OnColorPickerChangeListener l = new ColorPickerView.OnColorPickerChangeListener() {
            @Override
            public void onColorChanged(ColorPickerView picker, int color) {

                if (picker != top) {
                    top.setColors(Color.TRANSPARENT, color);
                }

                if (picker == center) {
                    left.setIndicatorColor(color);
                }

                show.setBackgroundColor(color);
                argb.setText("a: " + Color.alpha(color) + "  r: " + Color.red(color) + "  g: " + Color.green(color) + "  b: " + Color.blue(color));

            }

            @Override
            public void onStartTrackingTouch(ColorPickerView picker) {

            }

            @Override
            public void onStopTrackingTouch(ColorPickerView picker) {

            }
        };

        center.setOnColorPickerChangeListener(l);
        top.setOnColorPickerChangeListener(l);
        belowTop.setOnColorPickerChangeListener(l);
        right.setOnColorPickerChangeListener(l);
        left.setOnColorPickerChangeListener(l);

    }
}
