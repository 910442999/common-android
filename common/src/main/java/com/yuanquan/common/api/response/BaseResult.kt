package com.yuanquan.common.api.response

open class BaseResult<T> @JvmOverloads constructor(
    var message: String? = null,
    var msg: String? = null,
    var code: Int = 0,
    var data: T? = null,
)