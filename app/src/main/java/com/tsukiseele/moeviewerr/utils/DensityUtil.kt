package com.tsukiseele.moeviewerr.utils

import android.content.Context
import android.util.TypedValue

/**
 * 常用单位转换的辅助类
 */
class DensityUtil private constructor() {
    init {
        /** cannot be instantiated  */
        throw UnsupportedOperationException("cannot be instantiated")
    }

    companion object {

        fun getScreenWidth(context: Context): Int {
            val dm = context.resources.displayMetrics
            return dm.widthPixels
        }

        fun getScreenHeight(context: Context): Int {
            val dm = context.resources.displayMetrics
            return dm.heightPixels
        }

        fun getScreenSize(context: Context): IntArray {
            val dm = context.resources.displayMetrics
            return intArrayOf(dm.widthPixels, dm.heightPixels)
        }

        /**
         * dp转px
         *
         * @param context
         * @param dpVal
         * @return
         */
        fun dp2px(context: Context, dpVal: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.resources.displayMetrics
            ).toInt()  // Resources.getSystem().getDisplayMetrics();
        }

        /**
         * sp转px
         *
         * @param context
         * @param spVal
         * @return
         */
        fun sp2px(context: Context, spVal: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                spVal, context.resources.displayMetrics
            ).toInt()  // Resources.getSystem().getDisplayMetrics();
        }

        /**
         * px转dp
         *
         * @param context
         * @param pxVal
         * @return
         */

        fun px2dp(context: Context, pxVal: Float): Float {
            val scale = context.resources.displayMetrics.density
            return pxVal / scale
        }

        /**
         * px转sp
         *
         * @param context
         * @param pxVal
         * @return
         */

        fun px2sp(context: Context, pxVal: Float): Float {
            return pxVal / context.resources.displayMetrics.scaledDensity
        }
    }
}
