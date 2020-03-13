package com.tsukiseele.moeviewerr.ui.activitys

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.appcompat.widget.Toolbar
import android.view.View
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.ui.activitys.abst.BaseFragmentActivity
import com.tsukiseele.moeviewerr.ui.fragments.SettingsFragment

class SettingsActivity : BaseFragmentActivity() {
    private var toolbar: Toolbar? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        // 改变状态栏
        window.statusBarColor = resources.getColor(R.color.primary)

        initToolbar()

        fragmentManager
            .beginTransaction()
            .replace(R.id.activitySettings_FrameLayout,
                SettingsFragment()
            )
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            .commit()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun initToolbar() {
        toolbar = this.findViewById<View>(R.id.viewToolbar_Toolbar) as Toolbar
        toolbar!!.setTitleTextColor(Color.WHITE)
        toolbar!!.title = "首选项"
        toolbar!!.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_left_white)
        toolbar!!.setNavigationOnClickListener { onBackPressed() }
    }
}
