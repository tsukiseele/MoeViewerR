package com.tsukiseele.moeviewerr.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.App
import com.tsukiseele.moeviewerr.dataholder.GlobalObjectHolder
import com.tsukiseele.moeviewerr.libraries.BaseAdapter
import com.tsukiseele.moeviewerr.model.Image
import com.tsukiseele.moeviewerr.ui.activitys.ImageViewerActivity
import com.tsukiseele.moeviewerr.ui.adapter.ImageGridAdapter
import com.tsukiseele.moeviewerr.utils.Util
import com.tsukiseele.moeviewerr.utils.startActivityOfFadeAnimation
import com.tsukiseele.sakurawler.core.HtmlParser.CatalogLoadCallback
import com.tsukiseele.sakurawler.model.Catalog
import es.dmoral.toasty.Toasty
import java.util.*

class CatalogFragment : Fragment(), BaseAdapter.OnItemClickListener, CatalogLoadCallback<Image> {
    private var mLayout: View? = null
    private var mHandler: Handler? = null

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: BaseAdapter<Image>? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mImage: Image? = null
    private var mListColumn: Int = 0
    private var mCatalogLoadCallback: CatalogLoadCallback<Image>? = null

    val images: List<Image>?
        get() = mAdapter!!.list

    init {
        this.mHandler = Handler { msg ->
            when (msg.what) {
                STATE_OK -> {
                    mAdapter?.updateDataSet(msg.obj as List<Image>)
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout!!.isRefreshing)
                        mSwipeRefreshLayout!!.isRefreshing = false
                    setAutoTitle()
                }
                STATE_LOADING -> {
//                    mAdapter?.list?.also {
//
//                        it.addAll(msg.obj as MutableList<Image>)
//                        it.noti
//                    }
                    mAdapter?.addAll(msg.obj as MutableList<Image>)
                }
                STATE_ERROR -> Snackbar.make(
                    mLayout!!,
                    "Failed: " + msg.obj.toString(),
                    Snackbar.LENGTH_LONG
                ).show()
            }
            true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mLayout = inflater.inflate(R.layout.fragment_image_catalog, container, false)
        // 获取数据
        this.mImage = arguments?.getSerializable("mImage") as Image?

        this.mCatalogLoadCallback = context as CatalogLoadCallback<Image>
        // 初始化列表
        mRecyclerView =
            mLayout?.findViewById<View>(R.id.listImageCatalogFragment_RecyclerView) as RecyclerView
        mSwipeRefreshLayout =
            mLayout?.findViewById<View>(R.id.listImageCatalogFragment_SwipeRefreshLayout) as SwipeRefreshLayout
        // 初始化适配器
        val images = ArrayList<Image>()
        /*
        when (PreferenceHolder.getInt(PreferenceHolder.KEY_LISTTYPE, TYPE_GRID_3_COL)) {
            TYPE_FLOW_2_COL -> {
                mListColumn = 2
                mRecyclerView?.layoutManager =
                    StaggeredGridLayoutManager(mListColumn, StaggeredGridLayoutManager.VERTICAL)
                mAdapter = ImageStaggeredAdapter(
                    context!!,
                    images,
                    mListColumn
                )
            }
            TYPE_FLOW_3_COL -> {
                mListColumn = 3
                mRecyclerView?.layoutManager =
                    StaggeredGridLayoutManager(mListColumn, StaggeredGridLayoutManager.VERTICAL)
                mAdapter = ImageStaggeredAdapter(
                    context!!,
                    images,
                    mListColumn
                )
            }
            TYPE_GRID_3_COL -> {
                mListColumn = 3
                mRecyclerView?.layoutManager = GridLayoutManager(context, mListColumn)
                mAdapter =
                    ImageGridAdapter(context!!, images)
            }
            else -> {
                mListColumn = 3
                mRecyclerView?.layoutManager = GridLayoutManager(context, mListColumn)
                mAdapter =
                    ImageGridAdapter(context!!, images)
            }
        }*/
        mAdapter = ImageGridAdapter(context!!, images)
        mRecyclerView?.layoutManager = GridLayoutManager(context, 3)
        mRecyclerView?.adapter = mAdapter
        mAdapter?.setOnItemClickListener(this@CatalogFragment)

        mSwipeRefreshLayout?.setColorSchemeResources(*App.swipeRefreshColors!!)
        mSwipeRefreshLayout?.setProgressViewOffset(true, -100, 50)
        mSwipeRefreshLayout?.setOnRefreshListener { loadList() }
        // 开始加载列表
        loadList()

        return mLayout
    }

    override fun onItemClick(view: View, position: Int) {
        val image = images?.get(position) ?: return
        if ("video".equals(image.crawler?.site?.type) || image.getLowUrl()?.let {
                it.endsWith(".mp4") || it.endsWith(".webm") || it.endsWith(".m3u8")
            } ?: false) {
            Toasty.info(context!!, "此项为视频，请使用外部应用打开").show()
            Util.startSystemVideoPlayer(context!!, image.getLowUrl() ?: "")
        } else {
            val intent = Intent(context, ImageViewerActivity::class.java)
            GlobalObjectHolder.put("images", images!!)
            GlobalObjectHolder.put("images_index", position)
            activity?.startActivityOfFadeAnimation(intent)
        }
    }

    // 若标题为空，则自动生成标题
    fun setAutoTitle() {
        mAdapter?.list?.let {
            // 仅在项数大于1且最后一个元素的标题与父标题相同时执行
            if (it.size > 1 && it.last().title.equals(mImage?.title)) {
                it.forEachIndexed { index, image ->
                    image.title = (index + 1).toString()
                }
                mAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun loadList() {
        mSwipeRefreshLayout?.isRefreshing = true
        // 存在目录，则需要加载后才能获取列表
        if (mImage!!.hasCatalog())
            mImage?.let {
                Thread {
                    it.crawler
                        ?.parseOf(Image::class.java)
                        ?.parseCatalog(it, this@CatalogFragment)
                }.start()
            }
        else
            mHandler?.obtainMessage(STATE_OK, images)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mHandler = null
    }

    override fun onPageLoaded(catalog: Catalog<Image>, currentDatas: List<Image>, page: Int) {
        // 通知结果到ui
        mHandler?.obtainMessage(STATE_LOADING, currentDatas)?.sendToTarget()
        // 回调给活动
        mCatalogLoadCallback?.onPageLoaded(catalog, currentDatas, page)
    }

    override fun onSuccessful(catalog: Catalog<Image>) {
        // 通知结果到ui
        mHandler?.obtainMessage(STATE_OK, catalog)?.sendToTarget()
        // 回调给活动
        mCatalogLoadCallback?.onSuccessful(catalog)
    }

    override fun onFailed(catalog: Catalog<Image>, e: Throwable) {
        // 通知结果到ui
        mHandler?.obtainMessage(STATE_ERROR, e)?.sendToTarget()
        // 回调给活动
        mCatalogLoadCallback?.onFailed(catalog, e)
    }

    companion object {
        private val STATE_OK = 0
        private val STATE_LOADING = 1
        private val STATE_ERROR = 2
    }
}
