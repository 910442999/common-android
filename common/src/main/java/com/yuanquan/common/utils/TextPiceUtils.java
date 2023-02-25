package com.yuanquan.common.utils;

import android.text.TextUtils;

/**
 * Created by yjf
 *
 * @description:
 * @date :2019/12/9
 */
public class TextPiceUtils {

    /**
     * @param currency 币种
     * @param value    金额
     * @return
     */
    public static String changTVsize(String currency, String value) {
        return changTVsize(currency, value, "");
    }

    /**
     * @param currency 货币
     * @param value    金额
     * @param company  单位
     * @return
     */
    public static String changTVsize(String currency, String value, String company) {
        if (currency == null || currency.isEmpty()) {
            currency = "";
        }
        String money = "";
        if (!currency.contains("NT$")) {
            if (value.contains(".")) {
                money = currency + value;
            } else {
                money = currency + value;
            }
        } else {
            if (value.contains(".")) {
                money = currency + value.substring(0, value.indexOf("."));
            } else {
                money = currency + value;
            }
        }

        //        SpannableString spannableString = new SpannableString(money + (TextUtils.isEmpty(company) ? "" : ("/" + company)));
        //        spannableString.setSpan(new RelativeSizeSpan(1f), 0, currency.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //        if (money.contains(".")) {
        //            spannableString.setSpan(new RelativeSizeSpan(1f), money.indexOf("."), money.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //        }
        //        return spannableString;
        return money + (TextUtils.isEmpty(company) ? "" : ("/" + company));
    }


}
