package com.tsukiseele.moeviewerr.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.libraries.BaseAdapter
import com.tsukiseele.moeviewerr.libraries.BaseViewHolder
import me.shaohui.bottomdialog.BottomDialog

class DialogUtil {
    interface OnItemClickListener {
        fun onClick(view: View, position: Int)
    }

    companion object {
        fun showListDialog(
            fragmentManager: FragmentManager,
            items: MutableList<String>,
            listener: OnItemClickListener? = null
        ) : BottomDialog {
            val dialog = BottomDialog.create(fragmentManager) as BottomDialog
            return dialog.setLayoutRes(R.layout.view_simple_list)
                .setViewListener {
                    (it as RecyclerView).apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = object :
                            BaseAdapter<String>(
                                context!!,
                                items,
                                android.R.layout.simple_list_item_1
                            ) {
                            override fun convert(
                                context: Context,
                                holder: BaseViewHolder,
                                position: Int
                            ) {
                                holder.getView<TextView>(android.R.id.text1).apply {
                                    setText(items[position])
                                    setTextColor(resources.getColor(R.color.black))
                                }
                                listener?.let {
                                    holder.itemView.setOnClickListener {
                                        listener.onClick(holder.itemView, position)
                                        dialog.dismiss()
                                    }
                                }
                            }
                        }
                    }
                }.show() as BottomDialog
        }
    }
}