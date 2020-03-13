package com.tsukiseele.moeviewerr.utils

import android.graphics.BitmapFactory

object BitmapUtil {
    //在不加载图片情况下获取图片大小
    fun getBitmapWH(path: String): IntArray {
        val options = BitmapFactory.Options()
        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options) // 此时返回的bitmap为null
        /**
         * options.outHeight为原始图片的高
         */
        return intArrayOf(options.outWidth, options.outHeight)
    }

    //在不加载图片情况下获取图片大小
    fun getBitmapWH(bytes: ByteArray): IntArray {
        val options = BitmapFactory.Options()
        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) // 此时返回的bitmap为null
        /**
         * options.outHeight为原始图片的高
         */
        return intArrayOf(options.outWidth, options.outHeight)
    }
}
