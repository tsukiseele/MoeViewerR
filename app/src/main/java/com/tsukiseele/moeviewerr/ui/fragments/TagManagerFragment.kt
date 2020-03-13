package com.tsukiseele.moeviewerr.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.model.Tag
import com.tsukiseele.moeviewerr.dataholder.TagHolder
import com.tsukiseele.moeviewerr.ui.fragments.abst.BaseMainFragment
import com.tsukiseele.moeviewerr.ui.view.RecyclerViewDivider
import com.tsukiseele.moeviewerr.utils.DialogUtil
import com.tsukiseele.moeviewerr.utils.Util
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.frgament_tag_manager.*
import java.lang.StringBuilder

class TagManagerFragment : BaseMainFragment() {

    override val title: String
        get() = "标签"
    override val layoutId: Int
        get() = R.layout.frgament_tag_manager

    override fun onDestroyView() {
        TagHolder.instance.saveTags()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentTagManager_RecyclerView.adapter =
            TagAdapter(
                context!!,
                TagHolder.instance.tags
            )
        fragmentTagManager_RecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentTagManager_RecyclerView.addItemDecoration(RecyclerViewDivider(context!!))
        fragmentTagManager_FloatingActionButton.setOnClickListener {
            DialogUtil.showListDialog(
                fragmentManager!!,
                mutableListOf("导出所有内容到剪切板", "导出标签内容到剪切板", "导出JSON到剪切板"),
                object :
                    DialogUtil.OnItemClickListener {
                    override fun onClick(view: View, position: Int) {
                        when (position) {
                            0 ->
                                StringBuilder().apply {
                                    TagHolder.instance.tags.forEach {
                                        append(it.tag).append(": ").append(it.siteName).append("\n")
                                    }
                                }.toString().let {
                                    Util.putTextIntoClip(it)
                                }
                            1 ->
                                StringBuilder().apply {
                                    TagHolder.instance.tags.forEach {
                                        append(it.tag).append("\n")
                                    }
                                }.toString().let {
                                    Util.putTextIntoClip(it)
                                }
                            2 -> Util.putTextIntoClip(TagHolder.toJson())
                        }
                        Toasty.success(context!!, "已导出").show()
                    }
                })
        }
    }

    class TagAdapter(private val context: Context, private val tagList: MutableList<Tag>) :
        RecyclerView.Adapter<TagAdapter.ViewHolder>() {
        private val layoutInflater: LayoutInflater

        init {
            this.layoutInflater = LayoutInflater.from(context)
        }

        override fun onCreateViewHolder(p1: ViewGroup, p2: Int): ViewHolder {
            return ViewHolder(
                layoutInflater.inflate(R.layout.item_tag_info, p1, false)
            )
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, pos: Int) {
            viewHolder.item.setOnClickListener {
                AlertDialog.Builder(context)
                    .setItems(arrayOf("移除")) { p1, index ->
                        when (index) {
                            0 -> if (pos < tagList.size) {
                                tagList.removeAt(pos)
                                notifyItemRemoved(viewHolder.adapterPosition)
                                notifyItemRangeChanged(0, tagList.size - 1)
                            }
                        }
                    }.show()
            }
            val label = tagList[pos]
            viewHolder.label.text = label.tag
            if (label.siteName != null)
                viewHolder.siteName.text = label.siteName
        }

        override fun getItemCount(): Int {
            return tagList.size
        }

        class ViewHolder(var item: View) : RecyclerView.ViewHolder(item) {
            var label: TextView
            var siteName: TextView

            init {
                label = item.findViewById<View>(R.id.itemListLabelLayoutLabel_TextView) as TextView
                siteName =
                    item.findViewById<View>(R.id.itemListLabelLayoutSiteName_TextView) as TextView
            }
        }
    }
}
