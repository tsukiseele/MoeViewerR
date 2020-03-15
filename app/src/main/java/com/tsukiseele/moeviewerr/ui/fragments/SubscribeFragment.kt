package com.tsukiseele.moeviewerr.ui.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.tsukiseele.moeviewerr.MainActivity
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.model.Subscribe
import com.tsukiseele.moeviewerr.dataholder.SubscribeHolder
import com.tsukiseele.moeviewerr.libraries.BaseAdapter
import com.tsukiseele.moeviewerr.libraries.BaseViewHolder
import com.tsukiseele.moeviewerr.ui.fragments.abst.BaseMainFragment
import com.tsukiseele.moeviewerr.utils.*
import com.tsukiseele.sakurawler.SiteManager
import com.tsukiseele.sakurawler.utils.IOUtil
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_subscribe.*
import java.io.File
import java.io.IOException
import java.util.*

class SubscribeFragment : BaseMainFragment() {
    override val title: String
        get() = "订阅"
    override val layoutId: Int
        get() = R.layout.fragment_subscribe
    private var adapter: SubscribeAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SubscribeAdapter(context!!, SubscribeHolder.get())
        fragmentSubscribe_RecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentSubscribe_RecyclerView.adapter = adapter
        fragmentSubscribe_FloatingActionButton.setOnClickListener {
            showSubscribeDialog("添加订阅", options = {
                SubscribeHolder.get().add(it)
                SubscribeHolder.saveConfig()
                adapter!!.notifyDataSetChanged()
            })
        }
    }

    fun showSubscribeDialog(title: String, defaultName: String? = null, defaultUrl: String? = null, options: (Subscribe) -> Unit) {
        val itemView = View.inflate(context, R.layout.dialog_add_subscribe, null)
        val etSubscribeName =
            itemView.findViewById<TextInputEditText>(R.id.dialogAddSubscribeName_TextInputEditText)
        val etSubscribeUrl =
            itemView.findViewById<TextInputEditText>(R.id.dialogAddSubscribeUrl_TextInputEditText)
        defaultName?.let { etSubscribeName.setText(it) }
        defaultUrl?.let { etSubscribeUrl.setText(it) }
        etSubscribeName.requestFocus()
        AlertDialog.Builder(context!!)
            .setTitle(title)
            .setView(itemView)
            .setPositiveButton("确定", { dialog, which ->
                val subscribeName = etSubscribeName.text.toString()
                val subscribeUrl = etSubscribeUrl.text.toString()
                if (subscribeName.isBlank() || subscribeUrl.isBlank()) {
                    Toasty.warning(context!!, "订阅名和订阅源不能为空哦").show()
                    return@setPositiveButton
                }
                options(Subscribe(subscribeName, subscribeUrl, Date()))
            })
            .setNegativeButton("取消", null)
            .show()
    }

    fun syncSubscribe(url: String) {
        val mainActivity = (context as MainActivity)
        if (url.startsWith("http")) {
            val progress = ProgressDialog.show(
                context, "同步中", "请耐心等待...", true, false
            )
            /**
             * 载入规则
             */
            Thread {
                try {
                    val inputStream = OkHttpUtil.get(url).body()!!.byteStream()

                    IOUtil.writeBytes(
                        Config.DIR_SITE_PACK.toString() + File.separator + IOUtil.getUrlFileName(
                            url
                        ), inputStream
                    )
                    IOUtil.close(inputStream)

                    SiteManager.reloadSites(Config.DIR_SITE_PACK)
                    mainActivity.runOnUiThread {
                        mainActivity.drawerRightTreeAdapter!!
                            .updateDataSet(SiteManager.getSiteMap())
                        ToastUtil.showText("同步成功！")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    mainActivity.runOnUiThread { ToastUtil.showText("失败: $e") }
                }
                progress.dismiss()
            }.start()
        } else {
            val pack = File(url)
            if (pack.exists()) {
                try {
                    IOUtil.copyFile(pack, File(Config.DIR_SITE_PACK, pack.name))
                    SiteManager.loadSites(Config.DIR_SITE_PACK)
                    mainActivity.drawerRightTreeAdapter!!.updateDataSet(SiteManager.getSiteMap())
                    ToastUtil.showText("导入成功！")
                    mainActivity.openDefaultSite()
                } catch (e: IOException) {
                    ToastUtil.showText("导入失败: $e")
                }
            } else {
                ToastUtil.showText("文件不存在！")
            }
        }
    }

    inner class SubscribeAdapter(val context: Context, val subscribes: MutableList<Subscribe>) :
        BaseAdapter<Subscribe>(context, subscribes, R.layout.item_subscribe) {
        override fun convert(context: Context, holder: BaseViewHolder, position: Int) {
            val subscribe = subscribes[position]
            holder.setText(R.id.itemSubscribeName_TextView, subscribe.name)
            holder.setText(R.id.itemSubscribeUrl_TextView, subscribe.url)
            holder.getView<ImageView>(R.id.itemSubscribe_ImageView).setOnClickListener {
                syncSubscribe(subscribe.url)
            }
            holder.itemView.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle(subscribe.name)
                    .setItems(arrayOf("复制订阅名", "复制链接", "修改订阅"), { dialog, which ->
                        when (which) {
                            0 -> Util.putTextIntoClip(subscribe.name)
                            1 -> Util.putTextIntoClip(subscribe.url)
                            2 -> {
                                showSubscribeDialog("编辑订阅", subscribe.name, subscribe.url, {
                                    SubscribeHolder.get().set(position, it)
                                    SubscribeHolder.saveConfig()
                                    adapter!!.notifyDataSetChanged()
                                })
                            }
                        }
                    })
                    .show()
            }
            holder.itemView.setOnLongClickListener {
                Snackbar.make(container!!, "是否移除该订阅", Snackbar.LENGTH_SHORT)
                    .setAction("是的", {
                        SubscribeHolder.get().removeAt(position)
                        SubscribeHolder.saveConfig()
                        notifyDataSetChanged()
                    })
                    .show()
                true
            }
        }
    }
}
