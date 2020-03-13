package com.tsukiseele.moeviewerr.app

import android.os.Environment
import com.tsukiseele.moeviewerr.R
import java.io.File

object Config {
    /**
     * 目录配置，调用init()自动调用所有File类型字段的mkdirs()
     */
    // 内部储存目录
    var DIR_EXTERNAL_ROOT = Environment.getExternalStorageDirectory()
    // 程序数据目录
    val DIR_APP_DATA = File(DIR_EXTERNAL_ROOT,
        "/Android/data/" + R::class.java.getPackage()!!.name.trim())
    // 日志目录
    val DIR_LOGGER = File(DIR_APP_DATA, "logger")
    // 崩溃日志目录
    val DIR_CRASH_LOGGER = File(DIR_LOGGER, "crash")
    // 缓存数据目录
    val DIR_CACHE_DATA = File(DIR_APP_DATA, "cache")
    // 站点规则目录
    val DIR_SITE_RULE = File(DIR_APP_DATA, "rules")
    // 图片下载保存目录
    val DIR_IMAGE_DOWNLOAD =
        File(DIR_EXTERNAL_ROOT, Environment.DIRECTORY_PICTURES + "/MoeViewerR")
    /**
     * 文件路径配置
     */
    // 订阅数据路径
    val FILE_SUBSCRIBE = File(DIR_APP_DATA, "subscribe.json").absolutePath
    // 标签数据路径
    val FILE_TAG = File(DIR_APP_DATA, "label.json").absolutePath
    // 收藏夹数据路径
    val FILE_FAVORITES = File(DIR_APP_DATA, "favorites.json").absolutePath
    // 日志路径
    val FILE_LOGGER = File(DIR_LOGGER, "logger.txt").absolutePath
    /**
     * 反射创建Config类定义的所有目录
     */
    fun initAppDirectory(/*Context context*/) {
        try {
            val fields = Config::class.java.declaredFields
            for (field in fields) {
                val obj = field.get(null)
                if (obj is File && !obj.exists())
                    obj.mkdirs()
            }
        } catch (e: IllegalAccessException) {

        } catch (e: IllegalArgumentException) {

        }
    }
}
