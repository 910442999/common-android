package com.yuanquan.common.utils.pay

import android.app.Activity
import android.content.Context
import com.alipay.sdk.app.PayTask
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory

object PayUtils {
    /**
     * 调起微信支付
     * packageValue 可固定写死  Sign=WXPay
     */
    @JvmStatic
    fun toWXPay(
        context: Context, appId: String,
        partnerId: String,
        prepayId: String,
        nonceStr: String,
        timeStamp: String,
        packageValue: String,
        sign: String
    ) {
        val api = WXAPIFactory.createWXAPI(context, null)
        api.registerApp(appId)
        val payReq = PayReq()
        payReq.appId = appId
        payReq.partnerId = partnerId
        payReq.prepayId = prepayId
        payReq.nonceStr = nonceStr
        payReq.timeStamp = timeStamp
        payReq.packageValue = packageValue
        payReq.sign = sign
        api.sendReq(payReq)
    }


    /**
     * 支付宝支付
     *  必须异步调用
    val runable = Runnable {
    run {
    resultStatus 为  9000  支付成功
    result 为 同步返回需要验证的信息
    }
    }
    val payThread = Thread(runable)
    payThread.start()
     */
    @JvmStatic
    fun toALiPay(context: Activity, json: String): Map<String, String> {
        return PayTask(context).payV2(json, true)
    }
}