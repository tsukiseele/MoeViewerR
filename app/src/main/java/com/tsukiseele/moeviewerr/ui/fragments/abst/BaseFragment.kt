package com.tsukiseele.moeviewerr.ui.fragments.abst

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * 大部分Fragment的父类
 */
abstract class BaseFragment : Fragment() {
    var container: View? = null
        private set

    abstract val layoutId: Int

    open fun onCreateView(container: View, savedInstanceState: Bundle?) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.container = inflater.inflate(layoutId, container, false)
        this.container ?: throw NullPointerException()
        onCreateView(this.container as View, savedInstanceState)
        return this.container
    }
}
