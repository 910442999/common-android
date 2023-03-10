package com.yuanquan.common.widget;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZpPhoneEditText extends AppCompatEditText implements TextWatcher {
    // 特殊下标位置
    private static final int PHONE_INDEX_3 = 3;
    private static final int PHONE_INDEX_4 = 4;
    private static final int PHONE_INDEX_8 = 8;
    private static final int PHONE_INDEX_9 = 9;

    public ZpPhoneEditText(Context context) {
        super(context);
        initView();
    }

    public ZpPhoneEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZpPhoneEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    public void setFilters(String code) {
        if ("+86".equals(code)) {
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});//设置长度为11
        } else if ("+886".equals(code) || "+1".equals(code)) {
            setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});//设置长度为11
        }
    }
    private void initView() {
        setInputType(InputType.TYPE_CLASS_PHONE);
        setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned spanned, int dstart, int dend) {
                        if (" ".equals(source.toString()) || source.toString().contentEquals("\n") || dstart == 13) {
                            return "";
                        } else {
                            return null;
                        }
                    }
                }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        super.onTextChanged(s, start, before, count);
        if (s == null || s.length() == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i != PHONE_INDEX_3 && i != PHONE_INDEX_8 && s.charAt(i) == ' ') {
                continue;
            } else {
                sb.append(s.charAt(i));
                if ((sb.length() == PHONE_INDEX_4 || sb.length() == PHONE_INDEX_9) && sb.charAt(sb.length() - 1) != ' ') {
                    sb.insert(sb.length() - 1, ' ');
                }
            }
        }
        if (!sb.toString().equals(s.toString())) {
            int index = start + 1;
            if (sb.charAt(start) == ' ') {
                if (before == 0) {
                    index++;
                } else {
                    index--;
                }
            } else {
                if (before == 1) {
                    index--;
                }
            }

            setText(sb.toString());
            setSelection(index);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    // 获得不包含空格的手机号
    public String getPhoneText() {
        String str = getText().toString();
        return replaceBlank(str);
    }

    private String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            if (m.find()) {
                dest = m.replaceAll("");
            }
        }
        return dest;
    }
}
