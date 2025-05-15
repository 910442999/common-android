package com.yuanquan.common.api.error

open class ErrorResult @JvmOverloads constructor(
    var code: Int = 0,
    var errMsg: String? = "",
    var showLoading: Boolean = false,
    var showToast: Boolean = false,
    var index: Int = 0,//表示api类型（确定是哪个api）
    var method: String? = null//表示api方法（确定是哪个api）
)