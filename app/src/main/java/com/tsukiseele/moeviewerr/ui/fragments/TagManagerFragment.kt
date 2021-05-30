package com.tsukiseele.moeviewerr.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.dataholder.TagHolder
import com.tsukiseele.moeviewerr.model.Tag
import com.tsukiseele.moeviewerr.ui.fragments.abst.BaseMainFragment
import com.tsukiseele.moeviewerr.ui.view.RecyclerViewDivider
import com.tsukiseele.moeviewerr.utils.DialogUtil
import com.tsukiseele.moeviewerr.utils.AndroidUtil
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.frgament_tag_manager.*
import java.io.File
import kotlin.collections.LinkedHashMap

class TagManagerFragment : BaseMainFragment() {

    override val title: String
        get() = "标签"
    override val layoutId: Int
        get() = R.layout.frgament_tag_manager

    override fun onDestroyView() {
        TagHolder.instance.save()
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
                mutableListOf("从剪切板导入数据", "导出数据到剪切板"),
                object :
                    DialogUtil.OnItemClickListener {
                    override fun onClick(view: View, position: Int) {
                        when (position) {
                            0 -> {
                                val data = String(Base64.decode(AndroidUtil.getClipContent(), Base64.NO_WRAP.or(Base64.URL_SAFE)))

                                File(Config.FILE_DEBUG_LOG).appendText(data)

                                if (data.isEmpty() || data.isBlank()) {
                                    Toasty.error(context!!, "剪切板为空！").show()
                                } else {
                                    try {
                                        Gson().fromJson<LinkedHashMap<String, Tag>>(data,
                                            object : TypeToken<LinkedHashMap<String, Tag>>() {}.type).also {
                                            TagHolder.instance.addAll(it)
                                            TagHolder.instance.save()
                                            Toasty.success(context!!, "导入成功！").show()
                                        }
                                    } catch (e: Exception) {
                                        Toasty.error(context!!, "导入失败！未知的数据格式").show()
                                    }
                                }

//                                StringBuilder().apply {
//                                    TagHolder.instance.tags.forEach {
//                                        append(it.tag).append(": ").append(it.siteName).append("\n")
//                                    }
//                                }.toString().let {
//                                    Util.putTextIntoClip(it)
//                                }

                            }
                            1 -> {
                                AndroidUtil.putTextIntoClip(Base64.encodeToString(
                                    TagHolder.toJson().toByteArray(), Base64.NO_WRAP.or(Base64.URL_SAFE)))

                                Toasty.success(context!!, "已导出！").show()
                            }
                        }
                    }
                })
        }
    }

    class TagAdapter(private val context: Context, private val tags: LinkedHashMap<String, Tag>) :
        RecyclerView.Adapter<TagAdapter.ViewHolder>() {
        private val layoutInflater: LayoutInflater
        // 此处使用副本进行操作，修改数据后必须同步
        private val values = tags.toList().toMutableList()

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
                    .setItems(arrayOf("移除")) { _, index ->
                        when (index) {
                            0 -> if (pos < tags.size) {
                                // 同步移除
                                tags.remove(tags.toList().get(pos).first)
                                values.removeAt(pos)
                                notifyItemRemoved(viewHolder.adapterPosition)
                                notifyItemRangeChanged(0, tags.size - 1)
                            }
                        }
                    }.show()
            }
            val label = values[pos].second
            viewHolder.label.text = label.tag
            viewHolder.siteName.text = label.siteName
        }

        override fun getItemCount(): Int {
            return tags.size
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
