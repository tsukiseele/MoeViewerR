package com.tsukiseele.moeviewerr.ui.fragments

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tsukiseele.koradownload.KoraDownload
import com.tsukiseele.koradownload.base.DownloadTask
import com.tsukiseele.koradownload.base.Downloadable
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.libraries.BaseAdapter
import com.tsukiseele.moeviewerr.libraries.BaseViewHolder
import java.net.SocketTimeoutException
import java.util.ArrayList
import java.util.Collections

import com.tsukiseele.koradownload.SimpleTaskCallback
import com.tsukiseele.koradownload.base.DownloadTask.*
import com.tsukiseele.moeviewerr.app.App
import com.tsukiseele.moeviewerr.ui.fragments.abst.BaseMainFragment
import com.tsukiseele.sakurawler.utils.IOUtil
import es.dmoral.toasty.Toasty

class DownloadFragment : BaseMainFragment() {
    private var downloadListView: RecyclerView? = null
    private var downloadAdapter: BaseAdapter<Downloadable>? = null
    private var downloadTasks: List<Downloadable>? = null

    override val layoutId: Int
        get() = R.layout.fragment_download
    override val title: String
        get() = "下载"

    private fun bindView(container: View) {
        downloadListView = container.findViewById(R.id.pageDownloadFragment_RecyclerView)
    }

    override fun onCreateView(container: View, savedInstanceState: Bundle?) {
        bindView(container)
        downloadTasks = ArrayList<Downloadable>(KoraDownload.getTasks())
        downloadAdapter = DownloadAdapter(context!!, downloadTasks!!)
        downloadListView!!.adapter = downloadAdapter
        downloadListView!!.layoutManager = LinearLayoutManager(context)

        downloadAdapter!!.setOnItemClickListener(object : BaseAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val downloadable = downloadTasks!![position]
                if (downloadable is DownloadTask) {
                    when(downloadable.info().state) {
                        STATE_PROGRESS, STATE_START ->
                            KoraDownload.pause(downloadable)
                        STATE_PAUSE, STATE_ERROR ->
                            KoraDownload.resume(downloadable)
                    }
                } else {
                    //DownloadTasks tasks = (DownloadTasks) downloadable;
                }
            }
        })
        downloadAdapter!!.setOnLongItemClickListener(object : BaseAdapter.OnLongItemClickListener {
            override fun onLongItemClick(view: View, position: Int) {
                val downloadable = downloadTasks!![position]
                if (downloadable is DownloadTask) {
                    AlertDialog.Builder(context!!)
                        .setItems(arrayOf("重新下载", "取消下载", "复制链接")) { p1, position ->
                            when (position) {
                                0 -> KoraDownload.restart(downloadable)
                                1 -> {
                                    KoraDownload.cancel(downloadable)
                                    downloadAdapter!!.remove(position)
                                }
                                2 -> {
                                    val cm =
                                        context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.text = downloadable.info().url
                                    Toasty.info(context!!, "链接已复制")
                                }
                            }
                        }.show()
                } else {
                    //DownloadTasks tasks = (DownloadTasks) downloadable;
                }
            }
        })
    }

    inner class DownloadAdapter(
        context: Context,
        downloadTasks: List<Downloadable>
    ) : BaseAdapter<Downloadable>(
        context,
        Collections.synchronizedList(downloadTasks),
        R.layout.item_download
    ) {
        override fun convert(context: Context, holder: BaseViewHolder, position: Int) {
            val downloadable = downloadTasks!![position]
            if (downloadable is DownloadTask) {
                val tvTitle = holder.getView<TextView>(R.id.itemListDownloadLayoutTitle_TextView)
                val tvProgress = holder.getView<TextView>(R.id.itemListDownloadLayoutProgress_TextView)
                val pbProgress = holder.getView<ProgressBar>(R.id.itemListDownloadLayout_ProgressBar)
                // 封面和标题
                tvTitle.setText(IOUtil.getUrlFileName(downloadable.info().path.toString()))
                downloadable.info().getParam("cover")?.also {
                    Glide.with(App.context)
                        .load(it)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_image_off)
                        .centerCrop()
                        .into(holder.getView<ImageView>(R.id.itemListDownloadLayout_ImageView))
                }
                // 下载进度处理
                pbProgress.max = 100
                tvProgress.text = "等待连接"
                val handler = Handler { msg ->
                    when (msg.what) {
                        STATE_START -> tvProgress.text = "开始下载"
                        STATE_PROGRESS -> if (msg.obj is LongArray) {
                            val progress = msg.obj as LongArray
                            pbProgress.progress =
                                (progress[0] * 1.0 / progress[1] * 100).toInt()
                            tvProgress.text =
                                if (progress[1] > 0)
                                    IOUtil.formatDataSize(progress[0].toDouble()) + "/" +
                                            IOUtil.formatDataSize(progress[1].toDouble())
                                else
                                    IOUtil.formatDataSize(progress[0].toDouble()) + "/未知"
                        }
                        STATE_CANCEL -> {
                            tvProgress.text = "下载取消"
                            remove(position)
                        }
                        STATE_WAIT -> tvProgress.text = "等待下载"
                        STATE_SUCCESS -> {
                            pbProgress.visibility = View.INVISIBLE
                            tvProgress.text = "下载完成"
                            // 通知完成界面更新
//                            if (position < list!!.size)
//                                remove(position)
                        }
                        STATE_PAUSE -> tvProgress.text = "暂停"
                        STATE_ERROR -> tvProgress.text = if (msg.obj is SocketTimeoutException)
                                "超时：" + msg.obj.toString()
                            else
                                "失败：" + msg.obj.toString()
                    }
                    true
                }
                // 同步状态
                handler.sendEmptyMessage(downloadable.info().state)
                // 已完成的任务直接返回
                when(downloadable.info().state) {
                    STATE_SUCCESS -> return
                    STATE_CANCEL -> {
                        remove(position)
                        return
                    }
                }
                downloadable.info().callbacks.add(object : SimpleTaskCallback() {

                    override fun onFailed(task: DownloadTask, e: Throwable) {
                        val msg = Message.obtain()
                        msg.what = STATE_ERROR
                        msg.obj = e
                        handler.sendMessage(msg)
                        e.printStackTrace()
                    }

                    override fun onSuccessful(task: DownloadTask) {
                        handler.sendEmptyMessage(STATE_SUCCESS)
                    }

                    override fun onPause(task: DownloadTask) {
                        handler.sendEmptyMessage(STATE_PAUSE)
                    }

                    override fun onCancel(task: DownloadTask) {
                        handler.sendEmptyMessage(STATE_CANCEL)
                    }

                    override fun onStart(task: DownloadTask) {
                        handler.sendEmptyMessage(STATE_START)
                    }

                    override fun onProgress(task: DownloadTask) {
                        val msg = Message.obtain()
                        msg.what = STATE_PROGRESS
                        msg.obj = longArrayOf(task.info().currentLength, task.info().totalLength)
                        handler.sendMessage(msg)
                    }

                    override fun onFinish(task: DownloadTask) {

                    }
                })
            } else {

            }
        }
    }
}
