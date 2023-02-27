package com.yuanquan.common.event

open class EventMessage @JvmOverloads constructor(
    open var code: Int,
    open var obj: Any? = null
)