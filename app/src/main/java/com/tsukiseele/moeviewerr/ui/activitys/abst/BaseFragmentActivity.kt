package com.tsukiseele.moeviewerr.ui.activitys.abst

import androidx.fragment.app.FragmentActivity

open class BaseFragmentActivity: FragmentActivity() {
    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
