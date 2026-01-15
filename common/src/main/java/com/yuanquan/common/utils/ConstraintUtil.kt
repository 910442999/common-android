package com.yuanquan.common.utils

import android.transition.TransitionManager
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class ConstraintUtil(private val constraintLayout: ConstraintLayout) {

    private val resetConstraintSet = ConstraintSet().apply {
        clone(constraintLayout)
    }

    private lateinit var applyConstraintSet: ConstraintSet
    private var begin: ConstraintBegin? = null

    /**
     * 开始修改
     */
    fun begin(): ConstraintBegin {
        applyConstraintSet = ConstraintSet().apply {
            clone(constraintLayout)
        }
        return begin ?: ConstraintBegin().also {
            begin = it
        }
    }

    /**
     * 带动画的修改
     */
    fun beginWithAnim(): ConstraintBegin {
        TransitionManager.beginDelayedTransition(constraintLayout)
        return begin()
    }

    /**
     * 重置
     */
    fun reSet() {
        resetConstraintSet.applyTo(constraintLayout)
    }

    /**
     * 带动画的重置
     */
    fun reSetWidthAnim() {
        TransitionManager.beginDelayedTransition(constraintLayout)
        resetConstraintSet.applyTo(constraintLayout)
    }

    inner class ConstraintBegin {

        /**
         * 清除关系
         * 注意：这里不仅仅会清除关系，还会清除对应控件的宽高为 w:0,h:0
         */
        fun clear(@IdRes vararg viewIds: Int): ConstraintBegin {
            viewIds.forEach { viewId ->
                applyConstraintSet.clear(viewId)
            }
            return this
        }

        /**
         * 清除某个控件的，某个关系
         */
        fun clear(viewId: Int, anchor: Int): ConstraintBegin {
            applyConstraintSet.clear(viewId, anchor)
            return this
        }

        /**
         * 为某个控件设置 margin
         * @param viewId 某个控件ID
         * @param left marginLeft，默认0
         * @param top marginTop，默认0
         * @param right marginRight，默认0
         * @param bottom marginBottom，默认0
         */
        fun setMargin(
            @IdRes viewId: Int,
            left: Int = 0,
            top: Int = 0,
            right: Int = 0,
            bottom: Int = 0
        ): ConstraintBegin {
            setMarginLeft(viewId, left)
            setMarginTop(viewId, top)
            setMarginRight(viewId, right)
            setMarginBottom(viewId, bottom)
            return this
        }

        fun setMarginLeft(@IdRes viewId: Int, left: Int): ConstraintBegin {
            applyConstraintSet.setMargin(viewId, ConstraintSet.LEFT, left)
            return this
        }

        fun setMarginRight(@IdRes viewId: Int, right: Int): ConstraintBegin {
            applyConstraintSet.setMargin(viewId, ConstraintSet.RIGHT, right)
            return this
        }

        fun setMarginTop(@IdRes viewId: Int, top: Int): ConstraintBegin {
            applyConstraintSet.setMargin(viewId, ConstraintSet.TOP, top)
            return this
        }

        fun setMarginBottom(@IdRes viewId: Int, bottom: Int): ConstraintBegin {
            applyConstraintSet.setMargin(viewId, ConstraintSet.BOTTOM, bottom)
            return this
        }

        // 以下为连接关系的方法，名称改为符合Kotlin规范的小写驼峰
        fun leftToLeftOf(@IdRes startId: Int, @IdRes endId: Int): ConstraintBegin {
            applyConstraintSet.connect(startId, ConstraintSet.LEFT, endId, ConstraintSet.LEFT)
            return this
        }

        fun leftToRightOf(@IdRes startId: Int, @IdRes endId: Int): ConstraintBegin {
            applyConstraintSet.connect(startId, ConstraintSet.LEFT, endId, ConstraintSet.RIGHT)
            return this
        }

        fun topToTopOf(@IdRes startId: Int, @IdRes endId: Int): ConstraintBegin {
            applyConstraintSet.connect(startId, ConstraintSet.TOP, endId, ConstraintSet.TOP)
            return this
        }

        fun topToBottomOf(@IdRes startId: Int, @IdRes endId: Int): ConstraintBegin {
            applyConstraintSet.connect(startId, ConstraintSet.TOP, endId, ConstraintSet.BOTTOM)
            return this
        }

        fun rightToLeftOf(@IdRes startId: Int, @IdRes endId: Int): ConstraintBegin {
            applyConstraintSet.connect(startId, ConstraintSet.RIGHT, endId, ConstraintSet.LEFT)
            return this
        }

        fun rightToRightOf(@IdRes startId: Int, @IdRes endId: Int): ConstraintBegin {
            applyConstraintSet.connect(startId, ConstraintSet.RIGHT, endId, ConstraintSet.RIGHT)
            return this
        }

        fun bottomToBottomOf(@IdRes startId: Int, @IdRes endId: Int): ConstraintBegin {
            applyConstraintSet.connect(startId, ConstraintSet.BOTTOM, endId, ConstraintSet.BOTTOM)
            return this
        }

        fun bottomToTopOf(@IdRes startId: Int, @IdRes endId: Int): ConstraintBegin {
            applyConstraintSet.connect(startId, ConstraintSet.BOTTOM, endId, ConstraintSet.TOP)
            return this
        }

        fun setWidth(@IdRes viewId: Int, width: Int): ConstraintBegin {
            applyConstraintSet.constrainWidth(viewId, width)
            return this
        }

        fun setHeight(@IdRes viewId: Int, height: Int): ConstraintBegin {
            applyConstraintSet.constrainHeight(viewId, height)
            return this
        }

        fun setDimensionRatio(@IdRes viewId: Int, ratio: String): ConstraintBegin {
            applyConstraintSet.setDimensionRatio(viewId, ratio)
            return this
        }

        /**
         * 提交应用生效（作为扩展函数，使链式调用更自然）
         */
        fun commit() {
            applyConstraintSet.applyTo(constraintLayout)
        }
    }
}