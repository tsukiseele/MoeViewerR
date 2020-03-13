package com.tsukiseele.moeviewerr.ui.view

import android.util.Log
import android.view.View
import java.util.ArrayList
import java.util.Arrays
import java.util.HashSet
import com.zhy.view.flowlayout.FlowLayout

abstract class TagsAdapter<T> {
    private var mTagDatas: List<T>? = null
    private var mOnDataChangedListener: OnDataChangedListener? = null
    @Deprecated("")
    @get:Deprecated("")
    internal val preCheckedList = HashSet<Int>()


    val count: Int
        get() = if (mTagDatas == null) 0 else mTagDatas!!.size

    constructor(datas: List<T>) {
        mTagDatas = datas
    }

    @Deprecated("")
    constructor(datas: Array<T>) {
        mTagDatas = ArrayList(Arrays.asList(*datas))
    }

    internal interface OnDataChangedListener {
        fun onChanged()
    }

    internal fun setOnDataChangedListener(listener: OnDataChangedListener) {
        mOnDataChangedListener = listener
    }

    @Deprecated("")
    fun setSelectedList(vararg poses: Int) {
        val set = HashSet<Int>()
        for (pos in poses) {
            set.add(pos)
        }
        setSelectedList(set)
    }

    @Deprecated("")
    fun setSelectedList(set: Set<Int>?) {
        preCheckedList.clear()
        if (set != null) {
            preCheckedList.addAll(set)
        }
        notifyDataChanged()
    }

    fun notifyDataChanged() {
        if (mOnDataChangedListener != null)
            mOnDataChangedListener!!.onChanged()
    }

    fun getItem(position: Int): T {
        return mTagDatas!![position]
    }

    abstract fun getView(parent: FlowLayout, position: Int, t: T): View


    fun onSelected(position: Int, view: View) {
        Log.d("zhy", "onSelected $position")
    }

    fun unSelected(position: Int, view: View) {
        Log.d("zhy", "unSelected $position")
    }

    fun setSelected(position: Int, t: T): Boolean {
        return false
    }


}
