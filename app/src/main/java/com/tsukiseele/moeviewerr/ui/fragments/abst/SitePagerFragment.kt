package com.tsukiseele.moeviewerr.ui.fragments.abst

import com.tsukiseele.moeviewerr.interfaces.Titled

/**
 * 所有多页面Fragment的父类
 */
abstract class SitePagerFragment : BaseFragment(), Titled {
    abstract fun onDisplay()

    abstract fun onHide()
}
