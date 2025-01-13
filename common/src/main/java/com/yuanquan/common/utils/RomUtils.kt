package com.yuanquan.common.utils


import android.os.Build
import android.text.TextUtils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Date 2019/5/30 7:33 PM
 *
 * @author tangxin
 */

object RomUtils {

    val ROM_MIUI = "MIUI"
    val ROM_EMUI = "EMUI"
    val ROM_FLYME = "FLYME"
    val ROM_OPPO = "OPPO"
    val ROM_SMARTISAN = "SMARTISAN"
    val ROM_VIVO = "VIVO"
    val ROM_QIKU = "QIKU"

    private val KEY_VERSION_MIUI = "ro.miui.ui.version.name"
    private val KEY_VERSION_EMUI = "ro.build.version.emui"
    private val KEY_VERSION_OPPO = "ro.build.version.opporom"
    private val KEY_VERSION_SMARTISAN = "ro.smartisan.version"
    private val KEY_VERSION_VIVO = "ro.vivo.os.version"

    private var sRomName: String? = null
    private var sRomVersion: String? = null

    val isEmui: Boolean
        get() = check(ROM_EMUI)

    val isMiui: Boolean
        get() = check(ROM_MIUI)

    val isVivo: Boolean
        get() = check(ROM_VIVO)

    val isOppo: Boolean
        get() = check(ROM_OPPO)

    val isFlyme: Boolean
        get() = check(ROM_FLYME)

    val is360: Boolean
        get() = check(ROM_QIKU) || check("360")

    val isSmartisan: Boolean
        get() = check(ROM_SMARTISAN)

    val romName: String?
        get() {
            if (sRomName == null) {
                check("")
            }
            return sRomName
        }

    val romVersion: String?
        get() {
            if (sRomVersion == null) {
                check("")
            }
            return sRomVersion
        }

    fun check(rom: String): Boolean {
        if (sRomName != null) {
            return sRomName == rom
        }

        if (!TextUtils.isEmpty(getProp(KEY_VERSION_MIUI))) {
            sRomName = ROM_MIUI
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_EMUI))) {
            sRomName = ROM_EMUI
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_OPPO))) {
            sRomName = ROM_OPPO
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_VIVO))) {
            sRomName = ROM_VIVO
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_SMARTISAN))) {
            sRomName = ROM_SMARTISAN
        } else {
            sRomVersion = Build.DISPLAY
            if (sRomVersion!!.toUpperCase().contains(ROM_FLYME)) {
                sRomName = ROM_FLYME
            } else {
                sRomVersion = Build.UNKNOWN
                sRomName = Build.MANUFACTURER.toUpperCase()
            }
        }
        return sRomName == rom
    }

    fun getProp(name: String): String? {
        var line: String? = null
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $name")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            sRomVersion = null
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        sRomVersion = line
        return line
    }
}
