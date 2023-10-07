package com.yuanquan.common.widget.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager

/**
 *
 * @author  Lai
 *
 * @time 2021/3/1 16:23
 * @describe describe
 *
 */
abstract class BaseDialogFragment : AppCompatDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // setCancelable(false)
            //setCanceledOnTouchOutside(false)

            val dialogWindow = window!!
            val layoutParams = dialogWindow.attributes
            dialogWindow.decorView.setPadding(0, 0, 0, 0)
            layoutParams.gravity = Gravity.CENTER
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialogWindow.attributes = layoutParams

            setCancelable(isTouchClose())
            setCanceledOnTouchOutside(isTouchClose())
        }

        return inflater.inflate(getLayoutId(), container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view, savedInstanceState)
    }

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun initView(view: View, savedInstanceState: Bundle?)

    protected open fun isTouchClose(): Boolean {
        return true
    }
//    open fun isDialogShowing(): Boolean {
//        return if (mDialog != null && mDialog!!.isShowing) {
//            true
//        } else {
//            false
//        }
//    }
    override fun show(manager: FragmentManager, tag: String?) {
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }
}