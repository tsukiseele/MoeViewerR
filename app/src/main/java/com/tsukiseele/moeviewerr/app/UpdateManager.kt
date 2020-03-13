package com.tsukiseele.moeviewerr.app

import android.os.Handler
import android.os.Message
import com.tsukiseele.moeviewerr.utils.HttpUtil
import com.tsukiseele.moeviewerr.utils.IOUtil
import java.io.File

object UpdateManager {
    val UPDATE_SITE_RULE = 256

    private val SITE_RULE_UPDATE_URL =
        "https://raw.githubusercontent.com/TsukiSeele/Moe-Vewer_Sites/master/ExistsUpdateFlag"

    fun checkSiteRuleUpdate(handler: Handler) {
        Thread(Runnable {
            try {
                val data = HttpUtil.requestHtmlDocument(SITE_RULE_UPDATE_URL, 10000)
                val updateFlag = File(Config.DIR_APP_DATA, "rule_update.txt")
                if (!updateFlag.exists()) {
                    IOUtil.printText(updateFlag.absolutePath, data!!, "UTF-8")
                    Message.obtain(handler, UPDATE_SITE_RULE).sendToTarget()
                    return@Runnable
                }
                val text = updateFlag.readText()
                if (Integer.parseInt(data!!.trim { it <= ' ' }) > Integer.parseInt(text.trim { it <= ' ' })) {
                    IOUtil.printText(updateFlag.absolutePath, data, "UTF-8")
                    Message.obtain(handler, UPDATE_SITE_RULE).sendToTarget()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }
}
