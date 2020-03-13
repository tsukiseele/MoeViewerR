package com.tsukiseele.moeviewerr.utils

import android.widget.Toast
import android.content.Context
import com.tsukiseele.moeviewerr.app.*

object ToastUtil {
    private var toast: Toast? = null

    const val LENGTH_SHORT = Toast.LENGTH_SHORT
    const val LENGTH_LONG = Toast.LENGTH_LONG

    fun showText(message: String) {
        makeText(message)!!.show()
    }

    fun showText(message: String, duration: Int) {
        makeText(message, duration)!!.show()
    }

    fun showText(context: Context, message: String, duration: Int) {
        makeText(context, message, duration)!!.show()
    }

    @JvmOverloads
    fun makeText(message: String, duration: Int = ToastUtil.LENGTH_SHORT): Toast? {
        return makeText(App.context, message, duration)
    }

    fun makeText(context: Context, message: String, duration: Int): Toast? {
        if (toast == null) {
            toast = Toast.makeText(context, message, duration)
        } else {
            toast!!.setText(message)
            toast!!.duration = duration
        }
        return toast
    }
}// 单例模式
