package com.tsukiseele.moeviewerr.ui.activitys

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.dataholder.GlobalObjectHolder
import com.tsukiseele.moeviewerr.ui.activitys.abst.BaseActivity
import com.tsukiseele.moeviewerr.ui.fragments.GalleryFragment
import com.tsukiseele.moeviewerr.utils.getTitleTextView
import com.tsukiseele.sakurawler.model.Site
import es.dmoral.toasty.Toasty

class GalleryActivity : BaseActivity() {
    private var toolbar: Toolbar? = null
    private var fragment: GalleryFragment? = null

    private var site: Site? = null
    private var keywords: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        window.statusBarColor = resources.getColor(R.color.primary)

        // 获取传递过来的数据
        if (savedInstanceState == null) {
            site = GlobalObjectHolder.remove("site") as Site?
            keywords = GlobalObjectHolder.remove("keywords") as? String ?: ""
        } else {
            site = savedInstanceState.getSerializable("site") as Site
            keywords = savedInstanceState.getString("keywords") ?: ""
        }

        if (site == null) {
            Toasty.error(this, "site is null").show()
            finish()
            return
        }
        // 初始化
        bindViews()
        initToolbar()

        fragment = GalleryFragment(site!!, keywords ?: "")
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.activityGalleryContent_FrameLayout, fragment!!)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("site", site)
        outState.putString("keywords", keywords)
    }

    override fun onStart() {
        super.onStart()
        // 回调显示方法
        fragment?.onDisplay()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.activityGallery_Toolbar)
    }

    private fun initToolbar() {
        toolbar?.setBackgroundColor(resources.getColor(R.color.primary))
        toolbar?.setTitleTextColor(resources.getColor(R.color.white))
        toolbar?.title = keywords
        toolbar?.setNavigationIcon(R.drawable.ic_arrow_left_white)
        toolbar?.setNavigationOnClickListener { finish() }
        toolbar?.getTitleTextView()?.typeface = Typeface.DEFAULT
    }

    override fun finish() {
        site = null
        super.finish()
    }
}
