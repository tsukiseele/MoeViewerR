package com.tsukiseele.moeviewerr.ui.activitys

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.tsukiseele.koradownload.KoraDownload
import com.tsukiseele.koradownload.base.DownloadTask
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.dataholder.DownloadHolder
import com.tsukiseele.moeviewerr.dataholder.GlobalObjectHolder
import com.tsukiseele.moeviewerr.dataholder.PreferenceHolder
import com.tsukiseele.moeviewerr.dataholder.PreferenceHolder.KEY_LIST_TYPE
import com.tsukiseele.moeviewerr.model.Image
import com.tsukiseele.moeviewerr.ui.activitys.abst.BaseFragmentActivity
import com.tsukiseele.moeviewerr.ui.adapter.ImageGridAdapter.Companion.TYPE_GRID_3_COL
import com.tsukiseele.moeviewerr.ui.adapter.ImageStaggeredAdapter.Companion.TYPE_FLOW_3_COL
import com.tsukiseele.moeviewerr.ui.fragments.CatalogFragment
import com.tsukiseele.moeviewerr.utils.TextUtil
import com.tsukiseele.moeviewerr.utils.AndroidUtil
import com.tsukiseele.sakurawler.core.HtmlParser
import com.tsukiseele.sakurawler.model.Catalog
import com.tsukiseele.sakurawler.utils.IOUtil
import es.dmoral.toasty.Toasty
import java.io.File

class CatalogActivity : BaseFragmentActivity(), HtmlParser.CatalogLoadCallback<Image> {

    private var mToolbarLayout: CollapsingToolbarLayout? = null
    private var mToolbar: Toolbar? = null
    private var mIvToolbarImage: ImageView? = null
    private var mIvToolbarDownload: ImageView? = null
    private var mTvToolbarTitle: TextView? = null
    private var mTvToolbarInfo: TextView? = null

    private var mCatalogFragment: CatalogFragment? = null
    private var mImage: Image? = null

    private var mStatus: Int = 0

    private val mHandler = Handler { msg ->
        when (msg.what) {
            MSG_PAGELOADED -> {
                val catalog = msg.obj as List<Image>
                mTvToolbarInfo?.text = "已加载 ${catalog.size} 页"
            }
            MSG_SUCCESSFUL -> {
                val catalog = msg.obj as List<Image>
                mTvToolbarInfo?.text = "${catalog.size} 页"
            }

            MSG_FAILED -> {
                val objs = msg.obj as Array<Any>
                val catalog2 = objs[0] as List<Image>
                val e = objs[1] as Throwable

                mTvToolbarInfo?.text = if (!catalog2.isEmpty())
                    "Failed: 到 ${catalog2.size} 页" else "Failed: $e"
            }
        }
        true
    }

    override fun onPageLoaded(catalog: Catalog<Image>, datas: List<Image>, page: Int) {
        mHandler.obtainMessage(MSG_PAGELOADED, catalog).sendToTarget()
        mStatus = MSG_PAGELOADED
    }

    override fun onSuccessful(catalog: Catalog<Image>) {
        mHandler.obtainMessage(MSG_SUCCESSFUL, catalog).sendToTarget()
        mStatus = MSG_SUCCESSFUL
    }

    override fun onFailed(catalog: Catalog<Image>, e: Throwable) {
        mHandler.obtainMessage(MSG_FAILED, arrayOf<Any>(catalog, e)).sendToTarget()
        mStatus = MSG_FAILED
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("image_catalog", mImage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)
        window.statusBarColor = resources.getColor(R.color.primary)
        // 获取和恢复的数据
        if (savedInstanceState == null) {

            mImage = GlobalObjectHolder.remove("image_catalog") as? Image
        } else {
            mImage = savedInstanceState.getSerializable("image_catalog") as? Image
        }
        if (mImage == null){
            Toasty.error(this, "无效数据").show()
            finish()
        }
        // 初始化UI
        bindView()
        initToolbar()

        // 加载碎片
        mCatalogFragment = CatalogFragment()
        mCatalogFragment!!.arguments = Bundle().also {
            it.putSerializable("mImage", mImage)
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.activityCatalogContainer_FrameLayout, mCatalogFragment!!)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            .commit()

        mIvToolbarDownload?.setOnClickListener {
            when (mStatus) {
                MSG_SUCCESSFUL, MSG_FAILED -> {
                    val images = mCatalogFragment!!.images
                    val saveDir = File(Config.DIR_IMAGE_SAVE, mImage!!.title!!)

                    for (image in images!!) {

                        image.crawler?.parseOf(Image::class.java)
                            ?.parseExtraAsync(image, object : HtmlParser.ParsedCallback<Image> {
                                override fun onParsed(data: Image) {
                                    val url = image.getHighUrl()
                                    KoraDownload.newTask(url)
                                        .addHeaders(image.crawler!!.headers)
                                        .addParam("cover", image.coverUrl)
                                        .toFile(saveDir.absolutePath, IOUtil.getUrlFileName(url!!))
                                        .build()
                                        .execute()
                                }
                            })
                    }
                    Toasty.success(this, images.size.toString() + " 个任务已添加").show()
                }
            }
        }
    }

    private fun bindView() {
        mToolbarLayout = this.findViewById(R.id.activityCatalog_CollapsingToolbarLayout)
        mToolbar = this.findViewById(R.id.activityCatalog_Toolbar)
        mIvToolbarImage = this.findViewById(R.id.activityCatalogToolbar_ImageView)
        mTvToolbarTitle = this.findViewById(R.id.activityCatalogToolbarTitle_TextView)
        mTvToolbarInfo = this.findViewById(R.id.activityCatalogToolbarInfo_TextView)
        mIvToolbarDownload = this.findViewById(R.id.activityCatalogToolbarDownload_ImageView)
    }

    // 载入标题栏数据
    private fun initToolbar() {
        mToolbarLayout?.setCollapsedTitleTypeface(Typeface.DEFAULT)
        mToolbarLayout?.setExpandedTitleTypeface(Typeface.DEFAULT)
        mToolbarLayout?.setExpandedTitleColor(resources.getColor(R.color.translucent))
        mToolbar?.apply {
            title = mImage?.title ?: "无标题"
            navigationIcon = resources.getDrawable(R.drawable.ic_arrow_left_white)
            setNavigationOnClickListener { onBackPressed() }
        }
        mImage?.coverUrl?.let {
            val url = AndroidUtil.buildGlideUrl(it, mImage?.crawler?.headers)
            Glide.with(this)
                .load(url)
                .also {
                    if (PreferenceHolder.getInt(KEY_LIST_TYPE, TYPE_FLOW_3_COL) == TYPE_GRID_3_COL)
                        it.centerCrop()
                    else
                        it.fitCenter()
                }
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                .placeholder(R.drawable.ic_image_white)
//                .error(R.drawable.ic_image_off_white)
                .into(mIvToolbarImage!!)
        }

        mTvToolbarTitle?.text = mImage?.title
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            AlertDialog.Builder(this)
                .setItems(arrayOf("源网站", "下载所有")) { p1, pos ->
                    when (pos) {
                        0 -> {
                            val intent = Intent()
                            intent.action = Intent.ACTION_VIEW
                            intent.data = Uri.parse(mImage!!.catalogUrl)
                            startActivity(intent)
                        }
                        1 -> {
                            var url: String?
                            var name: String?
                            val dirName = mImage!!.title
                            for (image in mCatalogFragment!!.images!!) {
                                url = image.getHighUrl()
                                if (!TextUtil.isEmpty(url)) {
                                    name = IOUtil.getUrlFileName(url!!)
                                    DownloadHolder.instance!!.binder!!.execute(
                                        DownloadTask.Builder(url)
                                            .toFile(
                                                Config.DIR_IMAGE_SAVE.toString() + "/" + dirName + "/" + name,
                                                image.title
                                            )
                                            .build()
                                        /*, mImage.getCoverUrl()*/
                                    )
                                }
                            }
                            Toasty.info(this, "任务已添加").show()
                        }
                    }
                }.show()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    companion object {
        private val MSG_PAGELOADED = 0
        private val MSG_SUCCESSFUL = 1
        private val MSG_FAILED = 2
    }
}
