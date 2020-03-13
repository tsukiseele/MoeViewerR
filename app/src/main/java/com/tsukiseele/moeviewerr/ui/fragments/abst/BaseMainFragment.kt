package com.tsukiseele.moeviewerr.ui.fragments.abst

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.tsukiseele.moeviewerr.MainActivity
import com.tsukiseele.moeviewerr.R

/**
 * MainActivity中Fragment的父类
 */
abstract class BaseMainFragment : BaseFragment() {
    private val TOOLBAR_ID = R.id.viewToolbar_Toolbar

    abstract val title: String

    private var toolbar: Toolbar? = null

    private fun initToolbar(layout: View) {
        toolbar = layout.findViewById(TOOLBAR_ID)
        toolbar!!.title = title
        (context as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (context as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // getActivity().getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));

        initToolbar(container!!)

        val activity = context as MainActivity?

        // 绑定Toolbar到抽屉 ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(activity, activity!!.drawerLayout, toolbar, 0, 0)
        activity.drawerLayout!!.addDrawerListener(toggle)
        toggle.syncState()
    }
}
