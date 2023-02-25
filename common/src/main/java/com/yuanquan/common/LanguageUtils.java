package com.yuanquan.common;

import android.text.TextUtils;

import com.yuanquan.common.utils.AssetsUtil;
import com.yuanquan.common.utils.CommonUtils;
import com.yuanquan.common.utils.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class LanguageUtils {
    private static volatile JSONObject jsonObject;

    private LanguageUtils() {
    }

    private static JSONObject getLanguage() {
        if (jsonObject == null) {
            synchronized (LanguageUtils.class) {
                if (jsonObject == null) {

                    String json = SPUtils.getInstance().getString(getAppLanguageFileName());
                    if (TextUtils.isEmpty(json)) {
                        json = AssetsUtil.getTxtFromAssets(App.instance, getAppLanguageFileName());
                    }
                    try {
                        jsonObject = new JSONObject(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return jsonObject;
    }

    public static void setLanguage(String json) {
        SPUtils.getInstance().put(getAppLanguageFileName(), json);
    }

    public static String optString(String data) {
        return getLanguage().optString(data);
    }

    private static String getAppLanguageFileName() {
        return "zh.json";
    }

    public static void cleanLanguage() {
        jsonObject = null;
    }

}
