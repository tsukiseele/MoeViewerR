package com.tsukiseele.moeviewerr.libraries

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.util.SparseArray
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Checkable
import android.widget.ImageView
import android.widget.TextView

class BaseViewHolder(var container: View) : RecyclerView.ViewHolder(container) {
    private val mViews: SparseArray<View>

    init {
        mViews = SparseArray()
    }

    fun <T : View> getView(viewId: Int): T {
        var view: View? = mViews.get(viewId)
        if (view == null) {
            view = container.findViewById(viewId)
            mViews.put(viewId, view)
        }
        return view!! as T
    }

    fun setText(viewId: Int, resId: Int): BaseViewHolder {
        val textView = getView<TextView>(viewId)
        textView.setText(resId)
        return this
    }

    fun setText(viewId: Int, text: String): BaseViewHolder {
        val textView = getView<TextView>(viewId)
        textView.text = text
        return this
    }

    fun setText(viewId: Int, text: SpannableStringBuilder): BaseViewHolder {
        val textView = getView<TextView>(viewId)
        textView.text = text
        return this
    }

    fun setImageResource(viewId: Int, resId: Int): BaseViewHolder {
        val view = getView<ImageView>(viewId)
        view.setImageResource(resId)
        return this
    }

    fun setImageBitmap(viewId: Int, bitmap: Bitmap): BaseViewHolder {
        val view = getView<ImageView>(viewId)
        view.setImageBitmap(bitmap)
        return this
    }

    fun setImageDrawable(viewId: Int, drawable: Drawable): BaseViewHolder {
        val view = getView<ImageView>(viewId)
        view.setImageDrawable(drawable)
        return this
    }

    fun setBackgroundColor(viewId: Int, color: Int): BaseViewHolder {
        val view = getView<View>(viewId)
        view.setBackgroundColor(color)
        return this
    }

    fun setBackgroundResource(viewId: Int, backgroundRes: Int): BaseViewHolder {
        val view = getView<View>(viewId)
        view.setBackgroundResource(backgroundRes)
        return this
    }

    fun setTextColor(viewId: Int, textColor: Int): BaseViewHolder {
        val view = getView<TextView>(viewId)
        view.setTextColor(textColor)
        return this
    }


    @SuppressLint("NewApi")
    fun setAlpha(viewId: Int, value: Float): BaseViewHolder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            (getView(viewId) as View).alpha = value
        } else {
            // Pre-honeycomb hack to set Alpha value
            val alpha = AlphaAnimation(value, value)
            alpha.duration = 0
            alpha.fillAfter = true
            getView<View>(viewId).startAnimation(alpha)
        }
        return this
    }

    fun setVisible(viewId: Int, visible: Boolean): BaseViewHolder {
        val view = getView<View>(viewId)
        view.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }


    fun setTag(viewId: Int, tag: Any): BaseViewHolder {
        val view = getView<View>(viewId)
        view.tag = tag
        return this
    }

    fun setTag(viewId: Int, key: Int, tag: Any): BaseViewHolder {
        val view = getView<View>(viewId)
        view.setTag(key, tag)
        return this
    }

    fun setChecked(viewId: Int, checked: Boolean): BaseViewHolder {
        val view = getView<View>(viewId) as Checkable
        view.isChecked = checked
        return this
    }

    /**
     * 关于事件监听
     */
    fun setOnClickListener(viewId: Int, listener: View.OnClickListener): BaseViewHolder {

        val view = getView<View>(viewId)
        view.setOnClickListener(listener)
        return this
    }

    fun setOnTouchListener(viewId: Int, listener: View.OnTouchListener): BaseViewHolder {
        val view = getView<View>(viewId)
        view.setOnTouchListener(listener)
        return this
    }

    fun setOnLongClickListener(viewId: Int, listener: View.OnLongClickListener): BaseViewHolder {
        val view = getView<View>(viewId)
        view.setOnLongClickListener(listener)
        return this
    }
}
