package com.yuanquan.common.utils

/**
 * @Description: һЩ�Զ����׼����
 * @author zhangruiqian
 * @date 2023/4/25 10:33
 */
fun <T, R> T?.elif(block: (T) -> R, block2: () -> R): R {
    return if (this != null) {
        block(this)
    } else {
        block2()
    }
}