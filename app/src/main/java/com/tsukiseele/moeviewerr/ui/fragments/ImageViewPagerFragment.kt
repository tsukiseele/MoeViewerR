package com.tsukiseele.moeviewerr.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tsukiseele.koradownload.SimpleTaskCallback
import com.tsukiseele.koradownload.base.DownloadTask
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.App
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.model.Image
import com.tsukiseele.moeviewerr.model.Tag
import com.tsukiseele.moeviewerr.dataholder.BitmapCacheHolder
import com.tsukiseele.moeviewerr.dataholder.DownloadHolder
import com.tsukiseele.moeviewerr.dataholder.GlobalObjectHolder
import com.tsukiseele.moeviewerr.dataholder.TagHolder
import com.tsukiseele.moeviewerr.libraries.BaseAdapter
import com.tsukiseele.moeviewerr.libraries.BaseViewHolder
import com.tsukiseele.moeviewerr.ui.activitys.GalleryActivity
import com.tsukiseele.moeviewerr.ui.view.PageImageView
import com.tsukiseele.moeviewerr.ui.view.RoundProgressBar
import com.tsukiseele.moeviewerr.ui.view.TagsAdapter
import com.tsukiseele.moeviewerr.ui.view.TagsFlowLayout
import com.tsukiseele.moeviewerr.utils.*
import com.tsukiseele.sakurawler.core.HtmlParser
import com.tsukiseele.sakurawler.utils.IOUtil
import com.zhy.view.flowlayout.FlowLayout
import es.dmoral.toasty.Toasty
import me.shaohui.bottomdialog.BottomDialog
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*

class ImageViewPagerFragment : Fragment() {
    var mImage: Image? = null
        private set
    private var mIndex: Int? = null
        private set
    private var mLayout: View? = null
    private var mPinchImageView: PageImageView? = null
    private var mRoundProgressBar: RoundProgressBar? = null

    private var mBitmapCacheBytes: ByteArray? = null
    private var mImageLoadTask: ImageLoadTask? = null
    private var mBitmap: Bitmap? = null
    private var mCacheId: String? = null

    private var mSampleLength = "未知大小"
    private var mLargerLength = "未知大小"
    private var mOriginLength = "未知大小"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)     // 获取从活动传递过来的数据
        if ((mImage ?: (arguments?.get("image") as? Image)?.also { mImage = it }) == null) {
            Toasty.warning(context!!, "空数据").show()
            activity?.finish()
        }
        mIndex = mIndex ?: (arguments?.get("index") as? Int) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 加载布局
        mLayout = inflater.inflate(R.layout.view_image_viewer_page, container, false)

        mPinchImageView = mLayout!!.findViewById(R.id.pageImageViewerLayout_PinchImageView)
        mRoundProgressBar = mLayout!!.findViewById(R.id.pageImageViewerLayout_RoundProgressBar)
        // 生成缓存Id，如果有额外请求，那么URL可能会变化，则不能用URL作为key
        if (mImage!!.hasExtra())
            mCacheId = mImage!!.catalogUrl + "_" + mIndex
        else
            mCacheId = mImage!!.getLowUrl()

        // 加载缓存，如果没有缓存则进行网络请求
        if (!loadCache(mCacheId)) loadImage()

        mPinchImageView?.setOnLongClickListener {
            showDownloadDialog()
            true
        }
        return mLayout
    }

    /**
     * 加载图片
     *
     */
    fun loadImage() {
        if (mImage != null) {
            mImageLoadTask = ImageLoadTask().also {
                it.execute(mImage)
            }
        }
    }

    /**
     * 读取缓存图片
     * @param key 读取key的缓存
     */
    private fun loadCache(key: String?): Boolean {
        if (TextUtil.isEmpty(key))
            return false

        val bmp = BitmapCacheHolder.get(key)
        if (bmp != null) {
            activity?.runOnUiThread {
                mPinchImageView?.setImageBitmap(bmp)
                hideProgress()
            }
            return true
        }
        return false
    }

    /**
     * 下载对话框，以及处理下载
     */
    fun showDownloadDialog() {
        mImage?.apply {
            val urls = HashMap<String, String?>()
            if (!sampleUrl.isNullOrBlank())
                urls["$TYPE_SAMPLE: $mSampleLength"] = sampleUrl
            if (!largerUrl.isNullOrBlank())
                urls["$TYPE_LARGER: $mLargerLength"] = largerUrl
            if (!originUrl.isNullOrBlank())
                urls["$TYPE_ORIGIN: $mOriginLength"] = originUrl
            val keys = urls.keys.sorted().toMutableList()

            val dialog = BottomDialog.create(fragmentManager) as BottomDialog

            dialog.setLayoutRes(R.layout.view_simple_list)
                .setViewListener {
                    (it as RecyclerView).apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = object :
                            BaseAdapter<String>(
                                context!!,
                                keys,
                                android.R.layout.simple_list_item_1
                            ) {
                            override fun convert(
                                context: Context,
                                holder: BaseViewHolder,
                                position: Int
                            ) {
                                val key = keys[position]
                                holder.getView<TextView>(android.R.id.text1).apply {
                                    setText(key)
                                    setTextColor(resources.getColor(R.color.black))
                                }
                                holder.itemView.setOnClickListener {
                                    val url = urls[key]
                                    val dataOutPath = File(
                                        Config.DIR_IMAGE_DOWNLOAD,
                                        IOUtil.getUrlFileName(url!!)
                                    )
                                    // 存在缓存则直接保存
                                    if (url == getLowUrl() && mBitmapCacheBytes != null) {
                                        try {
                                            dataOutPath.writeBytes(mBitmapCacheBytes!!)
                                            // 通知图库更新
                                            Util.notifySystemImageUpdate(context, dataOutPath)

                                            Toasty.success(context, "保存成功").show()
                                        } catch (e: IOException) {
                                            Toasty.error(context, "保存失败：$e").show()
                                        }
                                    } else {
                                        DownloadHolder.instance?.binder?.execute(
                                            DownloadTask.Builder(url)
                                                .toFile(dataOutPath.absolutePath)
                                                .addHeaders(crawler!!.headers)
                                                .addParam("cover", coverUrl)
                                                .addCallback(object : SimpleTaskCallback() {
                                                    override fun onFailed(
                                                        task: DownloadTask?,
                                                        e: Throwable?
                                                    ) {
                                                    }

                                                    override fun onSuccessful(task: DownloadTask?) {
                                                        Util.notifySystemImageUpdate(
                                                            context,
                                                            task?.info()?.path
                                                        )
                                                    }
                                                })
                                                .build()
                                        )
                                        Toasty.success(context, "任务已添加").show()
                                    }
                                    dialog.dismiss()
                                }
                            }
                        }
                    }
                }.show()
        }
    }

    fun showImageInfo() {
        BottomDialog.create(fragmentManager)
            .setLayoutRes(R.layout.dialog_tags_flow)
            .setViewListener { view ->
                val tagsText = mImage!!.tags?.trim()
                if (!tagsText.isNullOrBlank()) {
                    /*
                    val tags = (fun (regex: String) : Array<String> {
                        return tagsText.split(regex.toRegex())
                            .dropLastWhile { it.isEmpty() }.toTypedArray()
                    }).let {
                        when {
                            tagsText.contains(',') -> it("""[,]+""")
                            tagsText.contains(';') -> it("""[;]+""")
                            else -> it("""[,;\s]+""")
                        }
                    }
                     */
                    // 将字符串形式的TAG分割成字符串数组
                    val tags = when {
                        tagsText.contains(",") -> tagsText.split(",")
                        tagsText.contains(";") -> tagsText.split(";")
                        else -> tagsText.split("""[,;\s]+""".toRegex())
                    }.dropLastWhile { it.isEmpty() }
                    if (tags.isEmpty())
                        return@setViewListener

                    view.findViewById<TextView>(R.id.dialogTagsFlowLayoutTip_TextView)
                        .setText("点击标签搜索，长按标签收藏")
                    // 创建标签流
                    val flTagsFlow =
                        view.findViewById<TagsFlowLayout>(R.id.dialogTagsFlowLayout_TagsFlowLayout)
                    flTagsFlow.adapter = object : TagsAdapter<String>(tags) {
                        override fun getView(
                            parent: FlowLayout,
                            position: Int,
                            t: String
                        ): View {
                            val item = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_tag, parent, false)
                            item.findViewById<TextView>(R.id.itemTagView_TextView).text =
                                t.trim()
                            return item
                        }
                    }
                    flTagsFlow.setOnTagClickListener(object :
                        TagsFlowLayout.OnTagClickListener {
                        override fun onTagClick(
                            view: View,
                            position: Int,
                            parent: FlowLayout
                        ): Boolean {
                            val intent = Intent(getContext(), GalleryActivity::class.java)
                            GlobalObjectHolder.put("site", mImage!!.crawler!!.site)
                            GlobalObjectHolder.put("keywords", tags[position])
                            activity?.startActivityOfFadeAnimation(intent)
                            return false
                        }
                    })
                    flTagsFlow.setOnTagLongClickListener(object :
                        TagsFlowLayout.OnTagLongClickListener {
                        override fun onTagLongClick(
                            view: View,
                            position: Int,
                            parent: FlowLayout
                        ): Boolean {
                            Toasty.success(context!!, "已添加: ${tags[position]}").show()
                            TagHolder.instance
                                .addTag(Tag(mImage!!.crawler!!.site.title, tags[position]))
                            TagHolder.instance.saveTags()
                            return true
                        }
                    })
                }
            }
            .show()
    }

    // 销毁数据
    override fun onDestroyView() {
        mPinchImageView?.setImageBitmap(null)
        mImageLoadTask?.also {
            if (!it.isCancelled)
                it.cancel(true)
        }
        mImageLoadTask = null
        mBitmap = null
        mBitmapCacheBytes = null
        super.onDestroyView()
    }

    // 隐藏进度
    private fun hideProgress() {
        mRoundProgressBar?.visibility = View.GONE
        mPinchImageView?.visibility = View.VISIBLE
    }

    // 显示进度
    private fun displayProgress(message: String) {
        mRoundProgressBar?.textContent = message
        mRoundProgressBar?.visibility = View.VISIBLE
        mPinchImageView?.visibility = View.GONE
    }

    // 并行请求网络文件大小
    private fun requestImagesSize() {
        val headers = mImage!!.crawler!!.headers
        val timeout = 5000
        Thread {
            if (!mImage?.sampleUrl.isNullOrBlank())
                mSampleLength = IOUtil.formatDataSize(
                    HttpUtil.getNetworkFileLength(
                        mImage?.sampleUrl!!, headers, timeout
                    ).toDouble()
                )
        }.start()
        Thread {
            if (!mImage?.largerUrl.isNullOrBlank())
                mLargerLength = IOUtil.formatDataSize(
                    HttpUtil.getNetworkFileLength(
                        mImage?.largerUrl!!, headers, timeout
                    ).toDouble()
                )
        }.start()
        Thread {
            if (!mImage?.originUrl.isNullOrBlank())
                mOriginLength = IOUtil.formatDataSize(
                    HttpUtil.getNetworkFileLength(
                        mImage?.originUrl!!, headers, timeout
                    ).toDouble()
                )
        }.start()
    }

    inner class ImageLoadTask : AsyncTask<Image, Int, Message>() {
        // 任务开始时调用
        override fun onPreExecute() {
            super.onPreExecute()
            displayProgress("Loading...")
        }

        // 后台操作
        override fun doInBackground(images: Array<Image>): Message? {
            // 请求图片信息
            val image = images[0]
            // 尝试加载缓存
            val hasCache = loadCache(mCacheId)
            // 加载扩展数据
            if (image.hasExtra()) {
                val parser = image.crawler?.parser as HtmlParser<Image>
                try {
                    parser.parseFillExtra(image)
                } catch (e: IOException) {
                    val msg = Message.obtain()
                    msg.what = MSG_ERROR
                    msg.obj = IOException("请求图像信息失败！", e)
                    return msg
                }
            }
            // 尝试请求各尺寸图片大小
            requestImagesSize()

            // 存在缓存则取消网络加载
            if (hasCache)
                return null

            // 开始请求图片
            var response: Response? = null
            var inputStream: InputStream? = null
            var baos: ByteArrayOutputStream? = null
            var bmp: Bitmap? = null
            val msg = Message.obtain()
            try {
                if (image.getLowUrl() == null) {
                    msg.what =
                        MSG_ERROR
                    msg.obj = "无效的URL: null"
                    return msg
                }
                response = OkHttpUtil[image.getLowUrl()!!, image.crawler!!.headers]
                if (response.isSuccessful) {
                    inputStream = response.body()!!.byteStream()
                    // 缓存为字节数组
                    baos = ByteArrayOutputStream()
                    val buff = ByteArray(8192)
                    var n: Int
                    val localLength = response.body()!!.contentLength().toInt()
                    var currentLength = 0
                    while (inputStream!!.read(buff).also { n = it } != -1) {
                        // 判断任务是否结束
                        if (isCancelled) {
                            inputStream.close()
                            baos.close()
                            response.close()
                            cancel(true)
                            return null
                        }
                        baos.write(buff, 0, n)
                        currentLength += n
                        publishProgress(localLength, currentLength)
                    }
                    mBitmapCacheBytes = baos.toByteArray()

                    bmp = BitmapFactory.decodeByteArray(
                        mBitmapCacheBytes,
                        0,
                        mBitmapCacheBytes!!.size
                    )
                } else {
                    msg.what =
                        MSG_ERROR
                    msg.obj = "connect failed: " + response.message()
                    return null
                }
                msg.what = MSG_OK
                msg.obj = bmp
            } catch (e: Exception) {
                msg.what = MSG_ERROR
                msg.obj = e
            } catch (e: OutOfMemoryError) {
                msg.what = MSG_OOM
            } finally {
                // 完毕后关闭链接，关闭流
                IOUtil.close(inputStream)
                IOUtil.close(baos)
                IOUtil.close(response)
            }
            return msg
        }

        // 更新时调用
        override fun onProgressUpdate(values: Array<Int>) {
            super.onProgressUpdate(*values)
            val localLength = values[0].toFloat()
            val currentLength = values[1].toFloat()

            // 如果服务器返回无效的文件长度，则放弃使用百分比
            if (localLength > 0) {
                val progress = currentLength * 100 / localLength
                mRoundProgressBar?.textContent = String.format("%.2f%%", progress)
                mRoundProgressBar?.progress = progress.toInt()
            } else {
                mRoundProgressBar?.textContent = IOUtil.formatDataSize(currentLength.toDouble())
            }
        }

        // 完成时调用
        override fun onPostExecute(result: Message?) {
            super.onPostExecute(result)
            // 隐藏加载进度
            hideProgress()

            if (isCancelled) {
                Toasty.info(App.context, "加载已取消").show()
                return
            }

            if (result != null) {
                when (result.what) {
                    MSG_OK -> {
                        if (result.obj == null) {
                            Toasty.error(App.context, "图像为空").show()
                            return
                        }
                        mBitmap = result.obj as Bitmap
                        val animation = AlphaAnimation(0.0f, 1.0f)
                        animation.duration = 500
                        mPinchImageView!!.setImageBitmap(mBitmap)
                        mPinchImageView!!.animation = animation
                        animation.start()
                        // 缓存数据
                        if (mBitmap != null && mCacheId != null)
                            BitmapCacheHolder.put(mCacheId, mBitmap)
                    }
                    MSG_OOM ->
                        Toasty.warning(
                            App.context,
                            "该图片过大，不支持预览，请下载后查看"
                        ).show()
                    MSG_ERROR -> if (result.obj != null) {
                        val message: String =
                            when (result.obj) {
                                is String -> result.obj as String
                                is SocketTimeoutException -> "超时"
                                is UnknownHostException -> "失败: 无效URL"
                                else -> "失败: ${result.obj}"
                            }
                        Toasty.error(App.context, message).show()
                    }
                }
            }
        }
    }

    companion object {
        private val TYPE_SAMPLE = "下载预览图"
        private val TYPE_LARGER = "下载大图"
        private val TYPE_ORIGIN = "下载原图"

        val MSG_OK = 0
        val MSG_OOM = 1
        val MSG_ERROR = 2

        fun newInstance(image: Image, index: Int): ImageViewPagerFragment {
            return ImageViewPagerFragment().also {
                val args = Bundle()
                args.putSerializable("image", image)
                args.putInt("index", index)
                it.arguments = args
            }
        }
    }
}
