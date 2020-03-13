package com.tsukiseele.moeviewerr.ui.view

import android.content.Context
import android.util.AttributeSet

class PageImageView : PinchImageView {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    /**
     * 计算双击之后图片接下来应该被缩放的比例
     *
     * 如果值大于getMaxScale或者小于fit center尺寸，则实际使用取边界值.
     * 通过覆盖此方法可以定制不同的图片被双击时使用不同的放大策略.
     *
     * @param innerScale 当前内部矩阵的缩放值
     * @param outerScale 当前外部矩阵的缩放值
     * @return 接下来的缩放比例
     *
     * @see .doubleTap
     * @see .getMaxScale
     */
    override fun calculateNextScale(innerScale: Float, outerScale: Float): Float {
        val currentScale = innerScale * outerScale
        val bitmapWidth = drawable.intrinsicWidth.toFloat()
        val bitmapHeight = drawable.intrinsicHeight.toFloat()
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        // 填充大小
        val scale: Float
        if (bitmapWidth / bitmapHeight > viewWidth / viewHeight)
            scale =
                if (bitmapHeight > viewHeight) bitmapHeight / viewHeight else viewHeight / bitmapHeight
        else
            scale =
                if (bitmapWidth > viewWidth) bitmapWidth / viewWidth else viewWidth / bitmapWidth

        return if (Math.abs(currentScale - innerScale) < 0.01)
            scale // 填充大小
        else if (Math.abs(currentScale - scale) < 0.01)
            maxScale // 最大缩放大小
        else
            innerScale // 适合大小
    }
}

