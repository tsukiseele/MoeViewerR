package com.tsukiseele.moeviewerr.interfaces

import android.view.View

abstract class CustomClickListener : View.OnClickListener {
    private var mLastClickTime: Long = 0
    private var mTimeInterval: Long

    constructor(interval: Long = 1000L) {
        this.mTimeInterval = interval
    }

    override fun onClick(v: View) {
        val nowTime = System.currentTimeMillis()
        if (nowTime - mLastClickTime > mTimeInterval) {
            // 单次点击事件
            onSingleClick()
            mLastClickTime = nowTime
        } else {
            // 快速点击事件
            onFastClick()
        }
    }

    protected abstract fun onSingleClick()
    protected abstract fun onFastClick()
}