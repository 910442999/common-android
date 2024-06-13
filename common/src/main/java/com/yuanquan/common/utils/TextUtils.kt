package com.yuanquan.common.utils

import android.text.TextUtils


object TextUtils {
    /**
     * @param currency 货币
     * @param value    金额
     * @param company  单位
     * @return
     */
    /**
     * @param currency 币种
     * @param value    金额
     * @return
     */
    @JvmStatic
    fun changTVsize(currency: String?, value: String, company: String = ""): String {
        var currency = currency
        if (currency == null || currency.isEmpty()) {
            currency = ""
        }
        var money = ""
        money = if (!currency.contains("NT$")) {
            if (value.contains(".")) {
                currency + value
            } else {
                currency + value
            }
        } else {
            if (value.contains(".")) {
                currency + value.substring(0, value.indexOf("."))
            } else {
                currency + value
            }
        }

        //        SpannableString spannableString = new SpannableString(money + (TextUtils.isEmpty(company) ? "" : ("/" + company)));
        //        spannableString.setSpan(new RelativeSizeSpan(1f), 0, currency.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //        if (money.contains(".")) {
        //            spannableString.setSpan(new RelativeSizeSpan(1f), money.indexOf("."), money.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //        }
        //        return spannableString;
        return money + if (TextUtils.isEmpty(company)) "" else "/$company"
    }

    @JvmStatic
    fun stripHtml(htmlString: String): String {
        return htmlString.replace("<[^>]+>".toRegex(), "")
    }
}
