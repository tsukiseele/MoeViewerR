package com.tsukiseele.moeviewerr.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.tsukiseele.moeviewerr.R

class RoundProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    /**
     * 圆弧的起始角度，参考canvas.drawArc方法
     */
    private var startAngle: Int = 0

    /**
     * 圆形内半径
     */
    private var radius: Int = 0

    /**
     * 进度条的宽度
     */
    private var ringWidth: Int = 0

    /**
     * 默认进度
     */
    // 进度改变时，需要通过invalidate方法进行重绘
    var progress = 0
        @Synchronized set(progress) {
            var progress = progress
            if (progress < 0) {
                progress = 0
            } else if (progress > 100) {
                progress = 100
            }
            field = progress
            postInvalidate()
        }

    /**
     * 圆形内部填充色
     */
    private var centerColor: Int = 0

    /**
     * 进度条背景色
     */
    private var ringColor: Int = 0

    /**
     * 进度条的颜色
     */
    private var progressColor: Int = 0

    /**
     * 文字大小
     */
    private var textSize: Int = 0

    /**
     * 文字颜色
     */
    private var textColor: Int = 0

    /**
     * 文字是否需要显示
     */
    private var isTextDisplay: Boolean = false

    var textContent: String? = null
        @Synchronized set(textContent) {
            field = textContent
            postInvalidate()
        }

    private var mPaint: Paint? = null

    init {

        // 获取自定义属性
        val a = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar)
        for (i in 0 until a.length()) {
            val attr = a.getIndex(i)
            when (attr) {
                R.styleable.RoundProgressBar_startAngle -> startAngle =
                    a.getInteger(attr, START_ANGLE)
                R.styleable.RoundProgressBar_centerColor -> centerColor =
                    a.getColor(attr, Color.parseColor(CENTER_COLOR))
                R.styleable.RoundProgressBar_progressColor -> progressColor =
                    a.getColor(attr, Color.parseColor(PROGRESS_COLOR))
                R.styleable.RoundProgressBar_ringColor -> ringColor =
                    a.getColor(attr, Color.parseColor(RING_COLOR))
                R.styleable.RoundProgressBar_textColor -> textColor =
                    a.getColor(attr, Color.parseColor(TEXT_COLOR))
                R.styleable.RoundProgressBar_textSize -> textSize = a.getDimension(
                    attr, TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE.toFloat(),
                        resources.displayMetrics
                    )
                ).toInt()
                R.styleable.RoundProgressBar_isTextDisplay -> isTextDisplay =
                    a.getBoolean(attr, true)
                R.styleable.RoundProgressBar_radius -> radius = a.getDimension(
                    attr, TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, CIRCLE_RADIUS.toFloat(),
                        resources.displayMetrics
                    )
                ).toInt()
                R.styleable.RoundProgressBar_ringWidth -> ringWidth = a.getDimension(
                    attr, TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, RING_WIDTH.toFloat(),
                        resources.displayMetrics
                    )
                ).toInt()
                else -> {
                }
            }
        }
        a.recycle()

        // 初始化画笔设置
        setPaint()
    }

    private fun setPaint() {
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 获取圆心坐标
        val cx = width / 2

        /**
         * 画圆心颜色
         */
        if (centerColor != 0) {
            drawInnerCircle(canvas, cx, cx)
        }

        /**
         * 画外层大圆
         */
        drawOuterCircle(canvas, cx, cx)

        /**
         * 画进度圆弧
         */
        drawProgress(canvas, cx, cx)

        /**
         * 画出进度百分比
         */
        drawProgressText(canvas, cx, cx)
    }

    private fun drawProgressText(canvas: Canvas, cx: Int, cy: Int) {
        if (!isTextDisplay || this.textContent.isNullOrEmpty()) {
            return
        }
        mPaint!!.color = textColor
        mPaint!!.textSize = textSize.toFloat()
        mPaint!!.typeface = Typeface.DEFAULT_BOLD
        mPaint!!.strokeWidth = 0f
        mPaint!!.style = Paint.Style.FILL
        val textWidth = mPaint!!.measureText(this.textContent)

        canvas.drawText(
            this.textContent!!,
            cx - textWidth / 2,
            (cy + textSize / 2).toFloat(),
            mPaint!!
        )
    }

    private fun drawProgress(canvas: Canvas, cx: Int, cy: Int) {
        mPaint!!.color = progressColor
        mPaint!!.strokeWidth = ringWidth.toFloat()
        mPaint!!.style = Paint.Style.STROKE
        val mRectF = RectF(
            (cx - radius).toFloat(),
            (cy - radius).toFloat(),
            (cx + radius).toFloat(),
            (cy + radius).toFloat()
        )
        val sweepAngle = (progress * 360.0 / 100).toFloat()
        canvas.drawArc(mRectF, startAngle.toFloat(), sweepAngle, false, mPaint!!)
    }

    private fun drawOuterCircle(canvas: Canvas, cx: Int, cy: Int) {
        mPaint!!.color = ringColor
        mPaint!!.strokeWidth = ringWidth.toFloat()
        mPaint!!.style = Paint.Style.STROKE
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), mPaint!!)
    }

    private fun drawInnerCircle(canvas: Canvas, cx: Int, cy: Int) {
        mPaint!!.color = centerColor
        mPaint!!.style = Paint.Style.FILL
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), (radius - ringWidth / 2).toFloat(), mPaint!!)
    }

    companion object {
        private val START_ANGLE = -90
        private val CENTER_COLOR = "#eeff06"
        private val RING_COLOR = "#FF7281E1"
        private val PROGRESS_COLOR = "#FFDA0F0F"
        private val TEXT_COLOR = "#FF000000"
        private val TEXT_SIZE = 30
        private val CIRCLE_RADIUS = 20
        private val RING_WIDTH = 5
    }
}
