package com.tsukiseele.moeviewerr.utils

import android.app.Activity
import android.content.*
import android.graphics.Point
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.App
import com.tsukiseele.moeviewerr.app.Constant
import com.tsukiseele.sakurawler.core.Const
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter


object AndroidUtil {
    /**
     * 获取异常的详细堆栈信息
     */
    fun getStackMessage(throwable: Throwable): String {
        var sw: StringWriter? = null
        var pw: PrintWriter? = null
        var msg: String? = null
        try {
            sw = StringWriter()
            pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            msg = sw.toString()
        } finally {
            pw!!.close()
        }
        return msg!!
    }

    /**
     * 构造Glide的请求头
     */
    fun buildGlideUrl(url: String, headers: Map<String, String>?): GlideUrl {
        // 导入请求头
        val builder = LazyHeaders.Builder()
        if (!headers.isNullOrEmpty())
            for ((key, value) in headers)
                builder.addHeader(key, value)
        return GlideUrl(url, builder.build())
    }

    /**
     * 插入到系统图库
     */

    fun insertSystemGallery(context: Context?, file: File?) {
        if (context == null || file == null || !file.exists())
            return
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        values.put(MediaStore.Images.Media.MIME_TYPE, Constant.MIME_TYPE_IMAGE_ALL)
        val uri = context.getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
    }

    /**
     * 判断列表是否滚动到底部
     */
    fun isSlideToBottom(recyclerView: RecyclerView?): Boolean {
        if (recyclerView == null)
            return false
        return if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange()) true else false
    }

    fun startSystemVideoPlayer(context: Context, url: String) {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        val mediaIntent = Intent(Intent.ACTION_VIEW)
        mediaIntent.setDataAndType(Uri.parse(url), mimeType)
        context.startActivity(mediaIntent)
    }

    fun setDrawerEdgeSize(
        activity: Activity?,
        drawerLayout: DrawerLayout?,
        direction: String,
        displayWidthPercentage: Float
    ) {
        if (activity == null || drawerLayout == null)
            return
        try {
            // 找到 ViewDragHelper 并设置 Accessible 为true
            val leftDraggerField =
                drawerLayout.javaClass.getDeclaredField("m" + direction + "Dragger")//Right
            leftDraggerField.isAccessible = true
            val leftDragger = leftDraggerField.get(drawerLayout) as ViewDragHelper

            // 找到 edgeSizeField 并设置 Accessible 为true
            val edgeSizeField = leftDragger.javaClass.getDeclaredField("mEdgeSize")
            edgeSizeField.isAccessible = true
            val edgeSize = edgeSizeField.getInt(leftDragger)

            // 设置新的边缘大小
            val displaySize = Point()
            activity.windowManager.defaultDisplay.getSize(displaySize)
            edgeSizeField.setInt(
                leftDragger,
                Math.max(edgeSize, (displaySize.x * displayWidthPercentage).toInt())
            )
        } catch (e: Exception) {

        }
    }

    /**
     * 导出文本到剪切板
     */
    fun putTextIntoClip(context: Context, label: String, text: String) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //创建ClipData对象
        val clipData = ClipData.newPlainText(label, text)
        //添加ClipData对象到剪切板中
        clipboardManager.setPrimaryClip(clipData)
    }

//    fun getClipboardContent(): String {
//        App.app?.also {
//            val cm = it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            val data = cm.primaryClip
//            if (data != null && data.itemCount > 0) {
//                val sequence = data.getItemAt(0)
//                sequence ?: return sequence.coerceToText(it).toString()
//            }
//        }
//        return ""
//    }
    fun getClipContent(): String {
        val manager = App.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (manager.hasPrimaryClip() && manager.primaryClip!!.itemCount > 0) {
            val addedText = manager.primaryClip!!.getItemAt(0).text
            val addedTextString = addedText.toString()
            if (addedTextString.isNotEmpty()) {
                return addedTextString
            }
        }
        return ""
    }

    fun putTextIntoClip(text: String) {
        putTextIntoClip(App.app!!, App.context.resources.getString(R.string.app_name), text)
    }
}
