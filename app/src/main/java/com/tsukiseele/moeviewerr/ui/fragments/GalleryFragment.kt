package com.tsukiseele.moeviewerr.ui.fragments

import android.content.Context
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
import com.tsukiseele.sakurawler.core.BaseCrawler
import com.tsukiseele.sakurawler.model.Site
import java.net.SocketTimeoutException

class GalleryFragment : SitePagerFragment {
    override val title: String
        get() = mSite!!.title!!

    override val layoutId: Int
        get() = R.layout.fragment_image_gallery

    private var mContext: Context? = null
    private var mHandler: Handler? = null

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: BaseAdapter<Image>? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private val mImages = ArrayList<Image>()
    private var mListLoader: ListLoader? = null
    private var mSite: Site? = null
    // 瀑布流列数
    private var mListColumn = 3
    // 加载参数
    private var mPageCode = DEFAULT_PAGECODE
    private var mKeywords = DEFAULT_KEYWORDS

    // 列表滚动监听
    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (Util.isSlideToBottom(recyclerView)) {
                loadResource(mKeywords)
            }
        }
    }

    constructor()

    constructor(site: Site) {
        this.mSite = site
    }

    constructor(site: Site, keywords: String) {
        this.mSite = site
        this.mKeywords = keywords
    }

    override fun onDisplay() {
        // 如果没有任何数据，则请求新的数据
        if (mImages.isEmpty()) {
            loadResource(mKeywords)
        }
    }

    override fun onHide() {

    }

    override fun onCreateView(container: View, savedInstanceState: Bundle?) {
        mSwipeRefreshLayout =
            container.findViewById<View>(R.id.listImageListFragment_SwipeRefreshLayout) as SwipeRefreshLayout
        mRecyclerView =
            container.findViewById<View>(R.id.listImageListFragment_RecyclerView) as RecyclerView
        mContext = getContext()

        // 初始化列表
        //mRecyclerView.addItemDecoration(new RecyclerViewDivider(mContext, 0));
        when (PreferenceHolder.getInt(PreferenceHolder.KEY_LISTTYPE, TYPE_FLOW_3_COL)) {
            TYPE_FLOW_2_COL -> {
                mListColumn = 2
                mRecyclerView?.layoutManager =
                    StaggeredGridLayoutManager(mListColumn, StaggeredGridLayoutManager.VERTICAL)
                mAdapter = mContext?.let {
                    ImageStaggeredAdapter(
                        it,
                        mImages,
                        mListColumn
                    )
                }
            }
            TYPE_FLOW_3_COL -> {
                mListColumn = 3
                mRecyclerView?.layoutManager =
                    StaggeredGridLayoutManager(mListColumn, StaggeredGridLayoutManager.VERTICAL)
                mAdapter = mContext?.let {
                    ImageStaggeredAdapter(
                        it,
                        mImages,
                        mListColumn
                    )
                }
            }
            TYPE_GRID_3_COL -> {
                mListColumn = 3
                mRecyclerView?.layoutManager = GridLayoutManager(mContext, mListColumn)
                mAdapter = mContext?.let {
                    ImageGridAdapter(
                        it,
                        mImages
                    )
                }
            }
            else -> {
                mListColumn = 3
                mRecyclerView?.layoutManager =
                    StaggeredGridLayoutManager(mListColumn, StaggeredGridLayoutManager.VERTICAL)
                mAdapter = mContext?.let {
                    ImageStaggeredAdapter(
                        it,
                        mImages,
                        mListColumn
                    )
                }
            }
        }
        mRecyclerView?.adapter = mAdapter
        mRecyclerView?.addOnScrollListener(onScrollListener)
        mRecyclerView?.isScrollbarFadingEnabled = true
        mAdapter?.setOnItemClickListener(object : CustiomItemClickListener() {
            override fun onItemSingleClick(view: View, position: Int) {
                val image = mImages[position]
                if (image.hasCatalog()) {
                    GlobalObjectHolder.put("image_catalog", mImages[position])
                    val intent = Intent(mContext, CatalogActivity::class.java)
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
                    val intent = Intent(mContext, ImageViewerActivity::class.java)
                    activity?.startActivityOfFadeAnimation(intent)
                }
            }
        })
        mAdapter?.setOnLongItemClickListener(object : BaseAdapter.OnLongItemClickListener {
            override fun onLongItemClick(view: View, position: Int) {
                val image = mImages[position]
                image.apply {
                    FavoritesHolder.add(this)
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
        mSwipeRefreshLayout?.setOnRefreshListener { loadResource(mKeywords) }

        mHandler = Handler { msg ->
            mListLoader = null
            // 隐藏顶部进度条
            mSwipeRefreshLayout?.let { if (it.isRefreshing) it.isRefreshing = false }
            // 执行操作
            when (msg.what) {
                STATE_OK -> {
                    if (msg.obj == null && view != null)
                        Snackbar.make(view!!, "获取数据为空", Snackbar.LENGTH_SHORT).show()

                    if (msg.obj is List<*>) {
                        val infoList = msg.obj as List<Image>
                        if (infoList.size > 0)
                            mAdapter?.addAll(infoList)
                        else
                            Toasty.info(App.context, "已经到底了").show()
                    }
                    mPageCode++
                }

                STATE_ERROR -> {
                    val e = msg.obj as Exception
                    val exceptionMsg: String = if (e is SocketTimeoutException) {
                        "加载超时：${mSite?.title}"
                    } else {
                        "加载失败：${mSite?.title}"
                    }
                    Log.e(GalleryFragment::class.java.name, e.message ?: "")

                    Toasty.error(App.context, exceptionMsg).show()
                }
            }
            true
        }
    }

    /**
     * 判断网络并加载资源
     * @param keywords 关键字
     * @param isRefresh 是否刷新链接
     */
    fun loadResource(keywords: String, isRefresh: Boolean = false) {
        if (App.isNetworkConnected(mContext)) {
            loadList(mPageCode, keywords, isRefresh)
        } else {
            Toasty.warning(App.context, "请检查网络连接").show()
        }
    }

    fun loadList(pageCode: Int, keywords: String, isRefresh: Boolean) {
        var page = pageCode
        if (mListLoader != null && isRefresh) {
            mListLoader!!.interrupt()
            mListLoader = null
        }
        if (mListLoader == null) {
            // 判断与之前搜索的内容是否属于同一内容
            if (this.mKeywords != keywords) {
                // 取消所有请求
                val glide = Glide.with(context!!)
                mRecyclerView?.children?.forEach {
                    glide.clear(it)
                }
                // 重置页码，设置新的关键字，清空列表
                page =
                    DEFAULT_PAGECODE
                this.mKeywords = keywords
                mAdapter?.clear()
            }

            if (!mSwipeRefreshLayout!!.isRefreshing)
                mSwipeRefreshLayout!!.isRefreshing = true
            // 取消图片流加载
            mListLoader = ListLoader(
                mHandler,
                mSite!!,
                page,
                keywords,
                ""
            )
            mListLoader!!.start()
        }
    }

    // 退出时销毁资源
    override fun onDestroyView() {
        if (mListLoader != null) {
            mListLoader!!.interrupt()
            mListLoader = null
        }
        mHandler = null
        super.onDestroyView()
    }

    class ListLoader(
        private var handler: Handler?,
        private val site: Site,
        private val pageCode: Int,
        private val keywords: String,
        private val extraKeys: String
    ) : Thread() {

        override fun interrupt() {
            super.interrupt()
            handler = null
        }

        override fun isInterrupted(): Boolean {
            return super.isInterrupted() || handler == null
        }

        private fun checkInterrupted() {

        }

        override fun run() {

            val crawler = Sakurawler(site)
            val mode = BaseCrawler.Mode()
            mode.type = if (keywords.isBlank()) BaseCrawler.MODE_HOME else BaseCrawler.MODE_SEARCH
            mode.pageCode = pageCode
            mode.keywords = keywords
            mode.extraKey = extraKeys
            crawler.mode(mode)
            val imageParser = crawler.parseOf(Image::class.java)
            if (isInterrupted) return
            try {
                val obj = imageParser.parseGallery()
                if (isInterrupted) return
                Message.obtain(
                    handler,
                    STATE_OK, obj
                ).sendToTarget()
            } catch (e: Exception) {
                if (isInterrupted) return
                Message.obtain(
                    handler,
                    STATE_ERROR, e
                ).sendToTarget()
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
/**
 * 判断网络并加载资源
 */
