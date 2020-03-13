package com.tsukiseele.moeviewerr.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import android.view.View
import android.widget.ImageView
import androidx.core.view.children
import com.bumptech.glide.Glide
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.App
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.model.Image
import com.tsukiseele.moeviewerr.dataholder.GlobalObjectHolder
import com.tsukiseele.moeviewerr.dataholder.FavoritesHolder
import com.tsukiseele.moeviewerr.dataholder.PreferenceHolder
import com.tsukiseele.moeviewerr.interfaces.CustiomItemClickListener
import com.tsukiseele.moeviewerr.libraries.BaseAdapter
import com.tsukiseele.moeviewerr.ui.activitys.CatalogActivity
import com.tsukiseele.moeviewerr.ui.adapter.ImageGridAdapter.Companion.TYPE_GRID_3_COL
import com.tsukiseele.moeviewerr.ui.activitys.ImageViewerActivity
import es.dmoral.toasty.Toasty
import java.util.ArrayList

import com.tsukiseele.moeviewerr.ui.adapter.ImageStaggeredAdapter.Companion.TYPE_FLOW_2_COL
import com.tsukiseele.moeviewerr.ui.adapter.ImageStaggeredAdapter.Companion.TYPE_FLOW_3_COL
import com.tsukiseele.moeviewerr.ui.adapter.ImageGridAdapter
import com.tsukiseele.moeviewerr.ui.adapter.ImageStaggeredAdapter
import com.tsukiseele.moeviewerr.ui.fragments.abst.SitePagerFragment
import com.tsukiseele.moeviewerr.utils.*
import com.tsukiseele.sakurawler.Sakurawler
import com.tsukiseele.sakurawler.model.Site
import java.io.File
import java.net.SocketTimeoutException

class GalleryFragment : SitePagerFragment {
    override val title: String
        get() = mSite!!.title!!

    override val layoutId: Int
        get() = R.layout.fragment_image_gallery

    private var mHandler: Handler? = null

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: BaseAdapter<Image>? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private val mImages = ArrayList<Image>()
    private var mGalleryLoader: GalleryLoader? = null
    private var mSakurawler: Sakurawler? = null
    private var mSite: Site? = null
    // 瀑布流列数
    private var mListColumn = 3
    // 加载参数
    private var mPageCode = DEFAULT_PAGECODE
    private var mKeywords = DEFAULT_KEYWORDS

    constructor()

    constructor(site: Site, keywords: String = DEFAULT_KEYWORDS, pageCode: Int = DEFAULT_PAGECODE) {
        this.mSite = site
        this.mKeywords = keywords
        this.mPageCode = pageCode
    }

    override fun onDisplay() {
        // 如果没有任何图片，则加载新的图片
        if (mImages.isEmpty()) loadGallery()
    }

    override fun onHide() {

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("site", mSite)
        outState.putString("keywords", mKeywords)
        outState.putInt("page_code", mPageCode)
    }

    override fun onCreateView(container: View, savedInstanceState: Bundle?) {
        savedInstanceState?.also {
            mSite = savedInstanceState.getSerializable("site") as? Site
            mKeywords = savedInstanceState.getString("keywords") ?: DEFAULT_KEYWORDS
            mPageCode = savedInstanceState.getInt("page_code")
        }
        mSakurawler = Sakurawler(mSite!!)

        mSwipeRefreshLayout =
            container.findViewById<View>(R.id.listImageListFragment_SwipeRefreshLayout) as SwipeRefreshLayout
        mRecyclerView =
            container.findViewById<View>(R.id.listImageListFragment_RecyclerView) as RecyclerView
        // 初始化列表
        when (PreferenceHolder.getInt(PreferenceHolder.KEY_LISTTYPE, TYPE_FLOW_3_COL)) {
            TYPE_FLOW_2_COL -> {
                mListColumn = 2
                mRecyclerView?.layoutManager =
                    StaggeredGridLayoutManager(mListColumn, StaggeredGridLayoutManager.VERTICAL)
                mAdapter = ImageStaggeredAdapter(context!!, mImages, mListColumn)
            }
            TYPE_FLOW_3_COL -> {
                mListColumn = 3
                mRecyclerView?.layoutManager =
                    StaggeredGridLayoutManager(mListColumn, StaggeredGridLayoutManager.VERTICAL)
                mAdapter = ImageStaggeredAdapter(context!!, mImages, mListColumn)
            }
            TYPE_GRID_3_COL -> {
                mListColumn = 3
                mRecyclerView?.layoutManager = GridLayoutManager(context, mListColumn)
                mAdapter = ImageGridAdapter(context!!, mImages)
            }
        }
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.isScrollbarFadingEnabled = true
        mRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (Util.isSlideToBottom(recyclerView))
                    loadGallery()
            }
        })
        mAdapter?.setOnItemClickListener(object : CustiomItemClickListener() {
            override fun onItemSingleClick(view: View, position: Int) {
                val image = mImages[position]
                if (image.hasCatalog()) {
                    GlobalObjectHolder.put("image_catalog", mImages[position])
                    val intent = Intent(context, CatalogActivity::class.java)
                    val imageView: ImageView? =
                        when (PreferenceHolder.getInt(PreferenceHolder.KEY_LISTTYPE, -1)) {
                            TYPE_FLOW_2_COL,
                            TYPE_FLOW_3_COL ->
                                view.findViewById(R.id.itemListImageStaggeredLayout_ImageView)
                            TYPE_GRID_3_COL ->
                                view.findViewById(R.id.itemListImageGridLayoutCover_ImageView)
                            else -> null
                        }
                    imageView?.also {
                        ActivityUtil.startActivityOfSceneTransition(activity!!, it, intent)
                    } ?: activity?.startActivityOfFadeAnimation(intent)
                } else {
                    GlobalObjectHolder.put("images", mImages)
                    GlobalObjectHolder.put("images_index", position)
                    val intent = Intent(context, ImageViewerActivity::class.java)
                    activity?.startActivityOfFadeAnimation(intent)
                }
            }
        })
        mAdapter?.setOnLongItemClickListener(object : BaseAdapter.OnLongItemClickListener {
            override fun onLongItemClick(view: View, position: Int) {
                val image = mImages[position]
                image.also {
                    FavoritesHolder.add(it)
                    FavoritesHolder.saveState()
                    Toasty.success(context!!, "已收藏").show()
                }
                /*
                val mImage = mImages[position]
                val selects = mutableListOf<String>().apply {
                    ObjectUtil.toMap(mImage).forEach{ entry ->
                        add("${entry.key}: ${entry.value ?: "none"}")
                    }
                }
                DialogUtil.showListDialog(fragmentManager!!, selects, object : DialogUtil.OnItemClickListener {
                        override fun onClick(view: View, position: Int) {
                            Util.putTextIntoClip(context!!, selects[position].let {
                                it.substring(it.indexOf(':') + 1).trim()
                            })
                            Toasty.success(context!!, "已复制").show()
                        }
                    })*/
            }
        })
        // 初始化下拉视图
        mSwipeRefreshLayout?.setColorSchemeResources(*App.swipeRefreshColors!!)
        mSwipeRefreshLayout?.setProgressViewOffset(true, -100, 50)
        mSwipeRefreshLayout?.setOnRefreshListener { loadGallery() }

        mHandler = Handler { msg ->
            mGalleryLoader = null
            // 隐藏顶部进度条
            mSwipeRefreshLayout?.let { if (it.isRefreshing) it.isRefreshing = false }
            // 执行操作
            when (msg.what) {
                STATE_OK -> {
                    msg.obj ?: Toasty.error(context!!,"空数据", Snackbar.LENGTH_SHORT).show()
                    if (msg.obj is List<*>) {
                        val images = msg.obj as List<Image>
                        if (images.isNotEmpty()) {
                            mAdapter?.addAll(images)
                        } else {
                            Toasty.info(App.context, "没有更多了").show()
                        }
                    }
                    mPageCode++
                }
                STATE_ERROR -> {
                    val e = msg.obj as Exception
                    val exceptionMsg: String = if (e is SocketTimeoutException)
                        "超时：${mSite?.title}"
                    else
                        "失败：${mSite?.title}"
                    Toasty.error(App.context, exceptionMsg).show()
                }
            }
            true
        }
    }

    // 搜索
    fun search(keywords: String, pageCode: Int = DEFAULT_PAGECODE) {
        // 与上次搜索的内容不同才进行搜索
        if (keywords != this.mKeywords) {
            // 取消上次所有预览图的加载
            try {
                val glide = Glide.with(context!!)
                mRecyclerView?.children?.forEach { glide.clear(it) }
                mAdapter?.clear()
            } catch (e: Exception) {}
            // 重置页码和关键字
            mPageCode = pageCode
            mKeywords = keywords
            loadGallery()
        }
    }

    /**
     * 加载画廊，每次执行自动加载下一页
     */
    fun loadGallery() {
        loadPage(mKeywords, mPageCode)
    }

    /**
     * 判断网络并加载资源
     * @param pageCode 页码
     * @param keywords 关键字
     */
    private fun loadPage(keywords: String, pageCode: Int) {
        if (App.isNetworkConnected(context)) {
            load(keywords, pageCode)
        } else {
            Toasty.warning(App.context, "请检查网络连接").show()
        }
    }

    /**
     * 加载资源
     * @param pageCode 页码
     * @param keywords 关键字
     */
    private fun load(keywords: String, pageCode: Int) {
        // 弹出加载视图
        mSwipeRefreshLayout?.also {
            if (!it.isRefreshing) it.isRefreshing = true
        }
        // 如果上次请求未完成，则取消该请求
        mGalleryLoader?.interrupt()
        // 开始加载资源
        mGalleryLoader = GalleryLoader(mSakurawler!!, mHandler, keywords, pageCode)
            .also { it.start() }
    }

    // 退出时销毁资源
    override fun onDestroyView() {
        mGalleryLoader?.also { it.interrupt() }
        mGalleryLoader = null
        mHandler = null
        super.onDestroyView()
    }

    // 画廊加载器
    class GalleryLoader(
        private val sakurawler: Sakurawler,
        private var handler: Handler?,
        private val keywords: String,
        private val pageCode: Int
    ) : Thread() {

        override fun interrupt() {
            super.interrupt()
            handler = null
        }

        override fun isInterrupted(): Boolean {
            return super.isInterrupted() || handler == null
        }

        override fun run() {
            sakurawler.pagecode(pageCode).keywords(keywords)
            val imageParser = sakurawler.parseOf(Image::class.java)
            if (isInterrupted) return
            try {
                val obj = imageParser.parseGallery()
                if (isInterrupted) return
                Message.obtain(handler, STATE_OK, obj).sendToTarget()
            } catch (e: Exception) {
                if (isInterrupted) return
                Message.obtain(handler, STATE_ERROR, e).sendToTarget()
            }
        }
    }

    companion object {
        val STATE_ERROR = 0
        val STATE_OK = 1

        val DEFAULT_PAGECODE = 1
        val DEFAULT_KEYWORDS = ""
    }
}