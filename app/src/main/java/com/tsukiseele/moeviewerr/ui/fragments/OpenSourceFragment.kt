package com.tsukiseele.moeviewerr.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tsukiseele.moeviewerr.R

class OpenSourceFragment : Fragment() {
    private var mContext: Context? = null

    private var layout: View? = null

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.id.fragmentOpenSourceContent_TextView, container, false)
        mContext = activity

        return layout
    }
}
