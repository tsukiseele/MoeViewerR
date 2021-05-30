package com.tsukiseele.moeviewerr.app

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import com.tsukiseele.moeviewerr.R
import java.io.File

object Config {
    /**
     * 目录配置，调用init()自动调用所有File类型字段的mkdirs()
     */
    // 内部储存目录
    var DIR_EXTERNAL_ROOT = ""
        private set
    // 程序数据目录
    //val DIR_APP_DATA = File(DIR_EXTERNAL_ROOT, "/Android/data/" + R::class.java.getPackage()!!.name.trim())
    var DIR_APP_DATA = File("")
    // 日志目录
    var DIR_LOGGER = File("")
        private set
    // 崩溃日志目录
    var DIR_CRASH_LOGGER = File("")
        private set
    // 缓存数据目录
    var DIR_CACHE_DATA = File("")
        private set
    // 站点规则目录
    var DIR_SITE_PACK = File("")
        private set
    // 图片下载保存目录
    var DIR_IMAGE_SAVE = File("")
        private set
    /**
     * 文件路径配置
     */
    // 订阅数据路径
    var FILE_SUBSCRIBE = ""
        private set
    // 标签数据路径
    var FILE_TAG = ""
        private set
    // 收藏夹数据路径
    var FILE_FAVORITES = ""
        private set
    //
    var FILE_DEBUG_LOG = ""
        private set

    /**
     * 初始化目录配置，自动调用所有File类型字段的mkdirs()
     */
    fun init(context: Context) {
        initAppDirectory(context)
        createDirectory()
    }

    /**
     * 目录配置
     */
    private fun initAppDirectory(context: Context) {
        // 内部储存目录
//        DIR_EXTERNAL_ROOT = Environment.getExternalStorageState()
        // 程序数据目录
        //val DIR_APP_DATA = File(DIR_EXTERNAL_ROOT, "/Android/data/" + R::class.java.getPackage()!!.name.trim())
        //DIR_APP_DATA = context.getExternalFilesDir(null)!!
        DIR_APP_DATA = ContextCompat.getExternalFilesDirs(context, null)[0]
        // 缓存数据目录
        DIR_CACHE_DATA = ContextCompat.getExternalCacheDirs(context)[0]
        // 日志目录
        DIR_LOGGER = File(DIR_APP_DATA, "logger")
        // 崩溃日志目录
        DIR_CRASH_LOGGER = File(DIR_LOGGER, "crash")
        // 站点规则目录
        DIR_SITE_PACK = File(DIR_APP_DATA, "rules")
        // 图片下载保存目录
//        DIR_IMAGE_DOWNLOAD =
//            File(DIR_EXTERNAL_ROOT, Environment.DIRECTORY_PICTURES + "/MoeViewerR")
        DIR_IMAGE_SAVE = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES + File.separator + context.resources.getString(R.string.app_name))

        /**
         * 文件路径配置
         */
        // 订阅数据路径
        FILE_SUBSCRIBE = File(DIR_APP_DATA, "subscribe.json").absolutePath
        // 标签数据路径
        FILE_TAG = File(DIR_APP_DATA, "tags.json").absolutePath
        // 收藏夹数据路径
        FILE_FAVORITES = File(DIR_APP_DATA, "favorites.json").absolutePath
        // 调试日志路径
        FILE_DEBUG_LOG = File(DIR_LOGGER, "debug.log").absolutePath
    }

    /**
     * 反射创建Config类定义的所有目录
     */
    private fun createDirectory() {
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
