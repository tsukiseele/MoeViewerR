package com.tsukiseele.moeviewerr.ui.activitys

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.ui.activitys.abst.BaseFragmentActivity
import com.tsukiseele.moeviewerr.ui.fragments.AboutFragment

class AboutActivity : BaseFragmentActivity() {
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        // 改变状态栏
        window.statusBarColor = resources.getColor(R.color.primary)

        initToolbar()

        fragmentManager
            .beginTransaction()
            .add(R.id.activityAbout_FrameLayout, AboutFragment())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    private fun initToolbar() {
        toolbar = this.findViewById<View>(R.id.viewToolbar_Toolbar) as Toolbar
        toolbar?.setTitleTextColor(Color.WHITE)
        toolbar?.title = "关于"
        toolbar?.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_left_white)
        toolbar?.setNavigationOnClickListener { onBackPressed() }
    }
}
