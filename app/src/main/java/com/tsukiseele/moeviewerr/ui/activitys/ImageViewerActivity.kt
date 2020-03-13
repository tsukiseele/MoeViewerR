package com.tsukiseele.moeviewerr.ui.activitys

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.model.Image
import com.tsukiseele.moeviewerr.dataholder.GlobalObjectHolder
import com.tsukiseele.moeviewerr.ui.activitys.abst.BaseFragmentActivity
import com.tsukiseele.moeviewerr.ui.fragments.ImageViewPagerFragment
import es.dmoral.toasty.Toasty
import java.io.Serializable
import java.util.*

class ImageViewerActivity : BaseFragmentActivity(), View.OnClickListener {
    private var toolbar: Toolbar? = null
    private var tvTitle: TextView? = null
    private var imageViewPager: ViewPager? = null
    // 底部操作按钮
    private var reloadButton: ImageButton? = null
    private var downloadButton: ImageButton? = null
    private var infoButton: ImageButton? = null

    private var currentFragment: ImageViewPagerFragment? = null
    // 数据
    private var images: List<Image>? = null
    private var currentIndex: Int? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("images", images as? Serializable)
        outState.putInt("currentIndex", currentIndex ?: 0)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.activityImageViewerReload_ImageButton -> currentFragment?.loadImage()
            R.id.activityImageViewerDownload_ImageButton -> currentFragment?.showDownloadDialog()
            R.id.activityImageViewerInfo_ImageButton -> currentFragment?.showImageInfo()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 获取传递过来的数据
        if (savedInstanceState == null) {
            images = GlobalObjectHolder.remove("images") as List<Image>?
            currentIndex = GlobalObjectHolder.remove("images_index") as? Int ?: 0
        } else {
            images = savedInstanceState.getSerializable("images") as List<Image>?
            currentIndex = savedInstanceState.getInt("currentIndex")
        }
        if (images == null) {
            Toasty.error(this, "数据异常").show()
            finish()
            return
        }
        // 透明状态栏
        setStatusColor()
        // UI操作
        setContentView(R.layout.activity_image_viewer)
        // 绑定视图
        bindView()
        // 初始化标题栏
        initToolbar()

        downloadButton?.setOnClickListener(this)
        reloadButton?.setOnClickListener(this)
        infoButton?.setOnClickListener(this)

        val fragmentList = ArrayList<Fragment>(images!!.size)
        images!!.forEachIndexed { index, image ->
            fragmentList.add(ImageViewPagerFragment.newInstance(image, index))
        }
        imageViewPager?.adapter =
            ImagePagerAdapter(
                fragmentList,
                supportFragmentManager,
                object :
                    ImagePagerAdapter.ImagePagerChangeListener {
                    override fun onChange(fragment: ImageViewPagerFragment, pos: Int) {
                        currentFragment = fragment
                        val currentImageInfo = currentFragment?.mImage
                        if (currentImageInfo != null && !TextUtils.isEmpty(currentImageInfo.title))
                            tvTitle?.text = currentImageInfo.title
                    }
                })
        imageViewPager?.currentItem = currentIndex ?: 0
        // 预加载左右3项
        imageViewPager?.offscreenPageLimit = 2
        // 翻页动画
        // imageViewPager!!.setPageTransformer(false, ZoomTransformer())
        // imageViewPager.setPageTransformer(false, new ScaleTransformer(0, 0.75F));
    }

    private fun bindView() {
        imageViewPager = this.findViewById<View>(R.id.activityImageViewer_ViewPager) as ViewPager
        reloadButton =
            this.findViewById<View>(R.id.activityImageViewerReload_ImageButton) as ImageButton
        downloadButton =
            this.findViewById<View>(R.id.activityImageViewerDownload_ImageButton) as ImageButton
        infoButton =
            this.findViewById<View>(R.id.activityImageViewerInfo_ImageButton) as ImageButton
    }

    private fun initToolbar() {
        toolbar = this.findViewById<View>(R.id.activityImageViewer_Toolbar) as Toolbar
        tvTitle = toolbar?.findViewById(R.id.toolbarTitle_TextView)
        toolbar?.setTitleTextColor(Color.WHITE)
        toolbar?.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_left_white)
        toolbar?.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setStatusColor() {
        // 透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val window = this.window
            val decorView = window.decorView
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            decorView.systemUiVisibility = option
            window.statusBarColor = resources.getColor(R.color.translucent)
        }
    }

    class ImagePagerAdapter(
        private val imagePagerList: List<Fragment>,
        private val fragmentManager: FragmentManager,
        private val imagePagerChangeListener: ImagePagerChangeListener?
    ) : FragmentStatePagerAdapter(fragmentManager) {

        interface ImagePagerChangeListener {
            fun onChange(fragment: ImageViewPagerFragment, pos: Int)
        }

        override fun getCount(): Int {
            return imagePagerList.size
        }

        override fun getItem(pos: Int): Fragment {
            return imagePagerList[pos]
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            imagePagerChangeListener?.onChange(`object` as ImageViewPagerFragment, position)
            super.setPrimaryItem(container, position, `object`)
        }
    }
}
