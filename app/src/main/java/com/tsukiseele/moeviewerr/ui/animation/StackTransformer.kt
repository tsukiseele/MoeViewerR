package com.tsukiseele.moeviewerr.ui.animation

import android.view.View
import androidx.viewpager.widget.ViewPager

class StackTransformer : ViewPager.PageTransformer {
    private val scale = .33f

    override fun transformPage(page: View, position: Float) {
        val scope = Math.abs(position)
        if (position >= -1.0f && position <= 0.0f) {
            // -1 >>> 0
            page.translationX = page.width * position
            page.elevation = 1f
        } else if (position > 0.0f) {
            // 1 >>> 0
            page.pivotX = 0f
            page.pivotY = (page.height / 2).toFloat()
            page.scaleX = 1 - scope * scale
            page.scaleY = 1 - scope * scale
            page.translationX = -page.width * position
            page.elevation = 0f
            page.alpha = 1 - scope
        }
    }
}
