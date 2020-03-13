package com.tsukiseele.moeviewerr.libraries

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.Collections
import java.util.ArrayList

/**
 * Created by RaphetS on 2016/9/28.
 * 普通的万能Adapter
 * 支持onItemClick
 * 支持onLongItemClick
 */
abstract class BaseAdapter<T>(
    private val mContext: Context,
    private var mDatas: MutableList<T>? = mutableListOf(),
    private val mLayoutId: Int
) : RecyclerView.Adapter<BaseViewHolder>() {
    private var mItemClickListener: OnItemClickListener? = null
    private var mLongItemClickListener: OnLongItemClickListener? = null

    val list: MutableList<T>?
        get() = mDatas

    protected abstract fun convert(context: Context, holder: BaseViewHolder, position: Int)

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    interface OnLongItemClickListener {
        fun onLongItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mItemClickListener = listener
    }

    fun setOnLongItemClickListener(listener: OnLongItemClickListener) {
        this.mLongItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false)
        return BaseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDatas!!.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        convert(mContext, holder, position)
        mItemClickListener?.also {
            holder.container.setOnClickListener { v ->
                it.onItemClick(v, position)
            }
        }
        mLongItemClickListener?.also {
            holder.itemView.setOnLongClickListener { v ->
                it.onLongItemClick(v, position)
                true
            }
        }
    }

    open fun updateDataSet(datas: List<T>) {
        mDatas = datas.toMutableList()
        notifyDataSetChanged()
    }

    open fun clear() {
        mDatas?.also {
            it.clear()
            notifyDataSetChanged()
        }
    }

    fun addAll(datas: List<T>) {
        mDatas?.also {
            it.addAll(datas)
            notifyItemRangeInserted(it.size - datas.size, datas.size)
            notifyItemRangeChanged(it.size - datas.size, datas.size)
        }
    }

    fun swap(fromPosition: Int, toPosition: Int) {
        mDatas?.also {
            Collections.swap(it, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            notifyItemChanged(fromPosition)
            notifyItemChanged(toPosition)
        }
    }

    fun add(data: T) {
        mDatas?.also {
            it.add(data)
            notifyItemInserted(it.size)
            notifyItemChanged(it.size)
        }
    }

    fun remove(position: Int) {
        mDatas?.also {
            it.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, it.size - position)
        }
    }

    fun insert(position: Int, data: T) {
        mDatas?.also {
            it.add(position, data)
            notifyItemInserted(position)
            notifyItemRangeChanged(position, it.size - position)
        }
    }
}

