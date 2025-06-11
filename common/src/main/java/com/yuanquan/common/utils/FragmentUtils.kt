package com.yuanquan.common.utils

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

/**
 * FragmentUtils - 提供安全添加和管理 Fragment 的工具方法
 *
 * 功能特性：
 * 1. 防止重复添加 Fragment
 * 2. 状态检查避免 IllegalStateException
 * 3. 支持替换模式
 * 4. 支持返回栈管理
 * 5. 支持自定义动画
 * 6. 包含错误恢复机制
 *
 * 使用示例
 *
 * 在 Activity 中使用
 *
 * class MainActivity : AppCompatActivity() {
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_main)
 *
 *         // 检查是否需要添加初始Fragment
 *         if (savedInstanceState == null) {
 *             val fragment = HomeFragment.newInstance()
 *             FragmentUtils.addFragmentSafely(
 *                 manager = supportFragmentManager,
 *                 containerId = R.id.fragment_container,
 *                 fragment = fragment,
 *                 tag = "home_fragment",
 *                 enterAnim = R.anim.slide_in_right,
 *                 exitAnim = R.anim.slide_out_left
 *             )
 *         }
 *     }
 *
 *     fun openSettings() {
 *         val settingsFragment = SettingsFragment.newInstance()
 *
 *         FragmentUtils.replaceFragmentSafely(
 *             manager = supportFragmentManager,
 *             containerId = R.id.fragment_container,
 *             fragment = settingsFragment,
 *             tag = "settings_fragment",
 *             enterAnim = R.anim.slide_in_bottom,
 *             exitAnim = R.anim.fade_out
 *         )
 *     }
 *
 *     fun openProfile() {
 *         // 防止重复添加相同的Fragment
 *         val profileTag = "profile_fragment"
 *
 *         if (FragmentUtils.isFragmentExists(supportFragmentManager, profileTag)) {
 *             // 如果已经存在，只需显示它
 *             FragmentUtils.showExistingFragment(
 *                 manager = supportFragmentManager,
 *                 tag = profileTag
 *             )
 *         } else {
 *             val profileFragment = ProfileFragment.newInstance()
 *
 *             FragmentUtils.addFragmentSafely(
 *                 manager = supportFragmentManager,
 *                 containerId = R.id.fragment_container,
 *                 fragment = profileFragment,
 *                 tag = profileTag,
 *                 enterAnim = android.R.anim.fade_in,
 *                 exitAnim = android.R.anim.fade_out
 *             )
 *         }
 *     }
 *
 *     override fun onBackPressed() {
 *         // 处理返回栈中的Fragment
 *         if (supportFragmentManager.backStackEntryCount > 0) {
 *             supportFragmentManager.popBackStack()
 *         } else {
 *             super.onBackPressed()
 *         }
 *     }
 * }
 *
 * 在 Fragment 中使用
 *
 * class HomeFragment : Fragment() {
 *
 *     companion object {
 *         fun newInstance() = HomeFragment()
 *     }
 *
 *     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
 *         return inflater.inflate(R.layout.fragment_home, container, false)
 *     }
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *
 *         view.findViewById<Button>(R.id.btn_details).setOnClickListener {
 *             openDetailsFragment()
 *         }
 *     }
 *
 *     private fun openDetailsFragment() {
 *         val detailsFragment = DetailsFragment.newInstance()
 *
 *         // 在Fragment中使用时需要父Activity的FragmentManager
 *         FragmentUtils.addFragmentSafely(
 *             manager = requireActivity().supportFragmentManager,
 *             containerId = R.id.fragment_container,
 *             fragment = detailsFragment,
 *             tag = "details_fragment",
 *             enterAnim = R.anim.slide_in_bottom
 *         )
 *     }
 * }
 *
 */
object FragmentUtils {

    // 扩展函数：安全提交事务
    private fun FragmentManager.safeCommit(
        allowStateLoss: Boolean = false,
        action: FragmentTransaction.() -> Unit
    ) {
        if (isStateSaved) {
            Log.w("FragmentUtils", "安全警告：FragmentManager 状态已保存，放弃提交事务")
            return
        }

        val transaction = beginTransaction()
        action(transaction)

        try {
            if (allowStateLoss) {
                transaction.commitAllowingStateLoss()
            } else {
                transaction.commit()
            }
        } catch (e: IllegalStateException) {
            Log.e("FragmentUtils", "提交事务时发生错误：${e.message}")
        }
    }

    /**
     * 安全添加 Fragment
     * @param manager FragmentManager 实例
     * @param containerId 容器视图 ID
     * @param fragment 要添加的 Fragment 实例
     * @param tag Fragment 的唯一标签
     * @param addToBackStack 是否添加到返回栈
     * @param allowStateLoss 是否允许状态丢失
     * @param enterAnim 进入动画资源 ID
     * @param exitAnim 退出动画资源 ID
     * @param popEnterAnim 弹出栈时的进入动画
     * @param popExitAnim 弹出栈时的退出动画
     */
    @JvmStatic
    fun addFragmentSafely(
        manager: FragmentManager,
        containerId: Int,
        fragment: Fragment,
        tag: String,
        addToBackStack: Boolean = true,
        allowStateLoss: Boolean = false,
        enterAnim: Int = 0,
        exitAnim: Int = 0,
        popEnterAnim: Int = 0,
        popExitAnim: Int = 0
    ) {
        manager.safeCommit(allowStateLoss) {
            // 设置自定义动画
            if (enterAnim != 0 || exitAnim != 0 || popEnterAnim != 0 || popExitAnim != 0) {
                setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
            }

            // 检查是否已存在相同标签的Fragment
            val existingFragment = manager.findFragmentByTag(tag)

            if (existingFragment != null) {
                // 将已存在Fragment置前
                show(existingFragment)

                // 隐藏同一容器中的其他Fragment（可选）
                hideOtherFragments(manager, containerId, existingFragment)
            } else {
                // 添加新的Fragment
                add(containerId, fragment, tag)
            }

            // 添加到返回栈
            if (addToBackStack) {
                addToBackStack(tag)
            }
        }
    }

    /**
     * 安全替换 Fragment（替换容器中所有内容）
     * @param manager FragmentManager 实例
     * @param containerId 容器视图 ID
     * @param fragment 要替换的 Fragment 实例
     * @param tag Fragment 的唯一标签
     * @param addToBackStack 是否添加到返回栈
     * @param allowStateLoss 是否允许状态丢失
     * @param enterAnim 进入动画资源 ID
     * @param exitAnim 退出动画资源 ID
     */
    @JvmStatic
    fun replaceFragmentSafely(
        manager: FragmentManager,
        containerId: Int,
        fragment: Fragment,
        tag: String,
        addToBackStack: Boolean = true,
        allowStateLoss: Boolean = false,
        enterAnim: Int = 0,
        exitAnim: Int = 0
    ) {
        manager.safeCommit(allowStateLoss) {
            // 设置自定义动画
            if (enterAnim != 0 || exitAnim != 0) {
                setCustomAnimations(enterAnim, exitAnim)
            }

            // 替换容器内容
            replace(containerId, fragment, tag)

            // 添加到返回栈
            if (addToBackStack) {
                addToBackStack(tag)
            }
        }
    }

    /**
     * 移除指定 Fragment
     * @param manager FragmentManager 实例
     * @param tag 要移除的 Fragment 标签
     * @param allowStateLoss 是否允许状态丢失
     */
    @JvmStatic
    fun removeFragmentSafely(
        manager: FragmentManager,
        tag: String,
        allowStateLoss: Boolean = false
    ) {
        val fragment = manager.findFragmentByTag(tag) ?: return

        manager.safeCommit(allowStateLoss) {
            remove(fragment)
        }
    }

    /**
     * 移除所有Fragment（除了指定的Fragment）
     * @param manager FragmentManager 实例
     * @param containerId 容器视图 ID
     * @param fragmentToKeep 要保留的Fragment实例
     */
    @JvmStatic
    fun removeAllFragmentsExcept(
        manager: FragmentManager,
        containerId: Int,
        fragmentToKeep: Fragment,
        allowStateLoss: Boolean = false
    ) {
        manager.safeCommit(allowStateLoss) {
            manager.fragments.forEach { fragment ->
                if (fragment.isAdded && fragment.id == containerId && fragment != fragmentToKeep) {
                    remove(fragment)
                }
            }
        }
    }

    /**
     * 隐藏同一容器中的其他Fragment
     */
    private fun FragmentTransaction.hideOtherFragments(
        manager: FragmentManager,
        containerId: Int,
        fragmentToShow: Fragment
    ) {
        manager.fragments.forEach { fragment ->
            if (fragment.isAdded && fragment.id == containerId && fragment != fragmentToShow) {
                hide(fragment)
            }
        }
    }

    /**
     * 检查Fragment是否已存在
     */
    @JvmStatic
    fun isFragmentExists(manager: FragmentManager, tag: String): Boolean {
        return manager.findFragmentByTag(tag) != null
    }

    /**
     * 显示已存在的Fragment
     */
    @JvmStatic
    fun showExistingFragment(
        manager: FragmentManager,
        tag: String,
        allowStateLoss: Boolean = false
    ) {
        val fragment = manager.findFragmentByTag(tag) ?: return

        manager.safeCommit(allowStateLoss) {
            show(fragment)
        }
    }
}