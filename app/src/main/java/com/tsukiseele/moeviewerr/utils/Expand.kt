package com.tsukiseele.moeviewerr.utils

import android.widget.TextView
import androidx.appcompat.widget.Toolbar

/**
 * 获取Toolbar中title的TextView
 */
fun Toolbar.getTitleTextView() : TextView? {
    try {
        val field = Toolbar::class.java.getDeclaredField("mTitleTextView")
        field.setAccessible(true)
        return field.get(this) as TextView?
    } catch (e: Exception) {}

    return null
}