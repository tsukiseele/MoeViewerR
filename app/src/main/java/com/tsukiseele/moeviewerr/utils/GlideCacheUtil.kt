package com.tsukiseele.moeviewerr.utils

import android.content.Context
import android.os.Looper

import com.bumptech.glide.Glide
import com.tsukiseele.sakurawler.utils.IOUtil

/**
 * Glide缓存工具类
 * Created by Trojx on 2017/12/10
 */

class GlideCacheUtil {

    /**
     * 清除图片磁盘缓存
     */
    fun clearImageDiskCache(context: Context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Thread(Runnable {
                    Glide.get(context).clearDiskCache()
                    // BusUtil.getBus().post(new GlideCacheClearSuccessEvent());
                }).start()
            } else {
                Glide.get(context).clearDiskCache()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 清除图片内存缓存
     */
    fun clearImageMemoryCache(context: Context) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context).clearMemory()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 清除图片所有缓存
     */
    fun clearImageAllCache(context: Context) {
        clearImageMemoryCache(context)
        Thread(Runnable {
            clearImageDiskCache(context)
            // 清空缓存文件
            val ImageExternalCatchDir = context.externalCacheDir
            IOUtil.deleteDirectoryAllFile(ImageExternalCatchDir)
        }).start()
    }

    /**
     * 获取Glide造成的缓存大小
     *
     * @return CacheSize
     */
    fun getCacheSize(context: Context): String {
        try {
            return IOUtil.formatDataSize(IOUtil.getDirectoryAllFileSize(context.externalCacheDir!!).toDouble())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    companion object {
        private var inst: GlideCacheUtil? = null

        val instance: GlideCacheUtil
            get() {
                if (inst == null) {
                    inst = GlideCacheUtil()
                }
                return inst!!
            }
    }
}
