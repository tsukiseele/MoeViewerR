package com.tsukiseele.moeviewerr.ui.animation

import androidx.viewpager.widget.ViewPager
import android.view.View

class ZoomTransformer : ViewPager.PageTransformer {
    private val scale = .33f

    override fun transformPage(page: View, position: Float) {
        val scope = Math.abs(position)
        if (position >= -1.0f && position <= 0.0f) {
            // -1 >>> 0
            page.scaleX = 1 - scope * scale
            page.scaleY = 1 - scope * scale
            page.translationX = -page.width * position
            page.alpha = 1 - scope
        } else if (position > 0.0f) {
            // 1 >>> 0
            page.scaleX = 1 - scope * scale
            page.scaleY = 1 - scope * scale
            page.translationX = -page.width * position
            page.alpha = 1 - scope

        }
    }
}
