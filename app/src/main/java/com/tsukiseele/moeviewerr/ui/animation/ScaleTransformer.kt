package com.tsukiseele.moeviewerr.ui.animation

import androidx.viewpager.widget.ViewPager
import android.view.View

class ScaleTransformer(private val scale: Float = .2f, private val alpha: Float = .2f) : ViewPager.PageTransformer {

    override fun transformPage(page: View, position: Float) {

        if (position < -1 || position > 1) {
            page.alpha = MAX_ALPHA
            page.scaleX = MAX_SCALE
            page.scaleY = MAX_SCALE
        } else if (position <= 1) { // [-1,1]
            val scaleX: Float
            if (position < 0)
                scaleX = MAX_SCALE + scale * position
            else
                scaleX = MAX_SCALE - scale * position
            page.scaleX = scaleX
            page.scaleY = scaleX
            page.alpha = 1 - alpha * Math.abs(position)
        }
    }

    companion object {
        private val MAX_ALPHA = 1f
        private val MAX_SCALE = 1f
    }
}
