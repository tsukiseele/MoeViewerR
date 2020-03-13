package com.tsukiseele.moeviewerr.dataholder

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.collection.LruCache
import com.jakewharton.disklrucache.DiskLruCache
import com.tsukiseele.moeviewerr.app.App
import com.tsukiseele.moeviewerr.app.Config
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

import com.tsukiseele.moeviewerr.utils.IOUtil.MB

object BitmapCacheHolder {
    private var diskLruCache: DiskLruCache? = null
    private var memoryLruCache: LruCache<String, Bitmap>? = null

    // 磁盘最大缓存大小(MB)
    val BITMAP_CACHE_SIZE = 128
    // 磁盘缓存图片质量(%)
    val BITMAP_QUALITY = 80
    // 内存最大缓存大小
    val MAX_MEMONRY_CACHE = (Runtime.getRuntime().maxMemory() / 4).toInt()

    init {
        try {
            memoryLruCache = object : LruCache<String, Bitmap>(MAX_MEMONRY_CACHE) {
                override fun sizeOf(key: String, bitmap: Bitmap): Int {
                    return bitmap.byteCount
                }
            }
            diskLruCache = DiskLruCache.open(
                Config.DIR_CACHE_DATA,
                App.packageInfo!!.versionCode,
                1,
                (BITMAP_CACHE_SIZE * MB).toLong()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun put(key: String?, value: Bitmap?) {
        if (key == null || value == null)
            return
        // 子线程进行IO操作
        Thread(Runnable {
            // 生成散列码
            val hashKey = key.hashCode().toString()
            // 尝试内存缓存
            if (memoryLruCache != null && memoryLruCache!!.get(hashKey) == null) {
                memoryLruCache!!.put(hashKey, value)
            }
            // 尝试本地缓存
            if (diskLruCache != null) {
                var editor: DiskLruCache.Editor? = null
                var os: OutputStream? = null
                try {
                    // 如果Key已存在则不缓存
                    if (diskLruCache!!.get(hashKey) != null)
                        return@Runnable
                    editor = diskLruCache!!.edit(hashKey)
                    if (editor != null) {
                        os = editor.newOutputStream(0)

                        val baos = ByteArrayOutputStream()
                        value.compress(Bitmap.CompressFormat.JPEG, BITMAP_QUALITY, baos)
                        val byteArray = baos.toByteArray()
                        os!!.write(byteArray)
                        os.flush()
                        editor.commit()
                        diskLruCache!!.flush()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        editor!!.abort()
                    } catch (ex: IOException) {
                    }

                } catch (oom: OutOfMemoryError) {

                } finally {
                    try {
                        os?.close()
                    } catch (e: IOException) {
                    }

                }
            }
        }).start()

    }

    operator fun get(key: String?): Bitmap? {
        if (key == null)
            return null
        val hashKey = key.hashCode().toString()
        var bitmap: Bitmap? = null
        // 尝试内存读取缓存
        if (memoryLruCache != null) {
            bitmap = memoryLruCache!!.get(hashKey)
            if (bitmap != null)
                return bitmap
        }
        // 尝试本地读取缓存
        if (diskLruCache != null) {
            var `is`: InputStream? = null
            var snapShot: DiskLruCache.Snapshot? = null
            try {
                snapShot = diskLruCache!!.get(hashKey)
                if (snapShot != null) {
                    `is` = snapShot.getInputStream(0)
                    bitmap = BitmapFactory.decodeStream(`is`)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (oom: OutOfMemoryError) {
                bitmap = null
            } finally {
                try {
                    `is`?.close()
                    snapShot?.close()
                } catch (e: IOException) {
                }

            }
        }
        return bitmap
    }

    fun size(): Long {
        return diskLruCache!!.size()
    }

}

