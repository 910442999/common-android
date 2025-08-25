package com.yuanquan.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.yuanquan.common.R;
import com.yuanquan.common.utils.KeyBoardUtils;

/**
 * Created by yjf
 *
 * @description:
 * @date :2019/11/29
 */
public class InputBottomDialog extends DialogFragment {

    Context context;
    private int maxLength = 0;
    private String textHint = "";
    private String textSend = "";
    public Dialog mDialog;
    private int inputType = 0;
    public InputBottomDialog(Context context) {
        this.context = context;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setTextHint(String textHint) {
        this.textHint = textHint;
    }

    public void setTextSend(String textSend) {
        this.textSend = textSend;
    }
    public void setInputType(int type) {
        inputType = type;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mDialog = super.onCreateDialog(savedInstanceState);
//        mDialog.setCanceledOnTouchOutside(false);
        Window window = mDialog.getWindow();
        if (window != null) {
            // 一定要设置Background，如果不设置，window属性设置无效,如果通过 window 设置宽高时，想要设置宽为屏宽，就必须调用下面这行代码。
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //全屏化对话框
            //        DisplayMetrics dm = new DisplayMetrics();
            //        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            //        WindowManager.LayoutParams params = win.getAttributes();
            //        params.gravity = Gravity.CENTER;
            // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
            //        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            //        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            //设置 DialogFragment 的进出动画
            //        params.windowAnimations = R.style.DialogAnimation;
            //        win.setAttributes(params);
        }
        return mDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;//设置宽度为铺满
        params.gravity = Gravity.BOTTOM;
        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getContext().getResources().getColor(android.R.color.transparent)));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_input_bottom_alert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDialog.setCanceledOnTouchOutside(true);
        KeyBoardUtils.openKeyboard(view, context);
        EditText etName = view.findViewById(R.id.et_name);
        TextView tvSelect = view.findViewById(R.id.tv_select);
        TextView tv_text_size = view.findViewById(R.id.tv_text_size);
        //        View v_view = view.findViewById(R.id.v_view);
        tvSelect.setText(textSend);
        if (maxLength > 0)
            etName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        etName.setHint(textHint);
        if (inputType != 0) {
            etName.setInputType(inputType);
        }
        etName.setFocusable(true);
        etName.requestFocus();
        etName.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_BACK)) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (maxLength > 0) {
                        tv_text_size.setText(s.length() + "/" + maxLength);
                    }
                    tvSelect.setEnabled(true);
                } else {
                    tvSelect.setEnabled(false);
                    if (maxLength > 0) {
                        tv_text_size.setText("0/" + maxLength);
                    }
                }
            }
        });
        tvSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSheetItemClickListener != null) {
                    String name = etName.getText().toString().trim();
                    onSheetItemClickListener.onClick(name);
                }
                KeyBoardUtils.closeKeyboard(view, context);
                dismiss();
            }
        });
        //        v_view.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                KeyBoardUtils.closeKeybord(view, context);
        //                dialog.dismiss();
        //            }
        //        });
    }

    public OnSheetItemClickListener onSheetItemClickListener;

    public InputBottomDialog setOnSheetItemClickListener(OnSheetItemClickListener onSheetItemClickListener) {
        this.onSheetItemClickListener = onSheetItemClickListener;
        return this;
    }

    public interface OnSheetItemClickListener {
        void onClick(String text);
    }
}
