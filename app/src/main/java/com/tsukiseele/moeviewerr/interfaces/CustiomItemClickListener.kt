package com.tsukiseele.moeviewerr.interfaces

import android.view.View
import com.tsukiseele.moeviewerr.libraries.BaseAdapter

abstract class CustiomItemClickListener(interval: Long = 1000L) : BaseAdapter.OnItemClickListener {
    private var mLastClickTime: Long = 0
    private var mTimeInterval: Long = interval

    override fun onItemClick(view: View, position: Int) {
        val nowTime = System.currentTimeMillis()
        if (nowTime - mLastClickTime > mTimeInterval) {
            // 单次点击事件
            onItemSingleClick(view, position)
            mLastClickTime = nowTime
        } else {
            // 快速点击事件
            onItemFastClick(view, position)
        }
    }

    abstract fun onItemSingleClick(view: View, position: Int)
    fun onItemFastClick(view: View, position: Int) {}
}
