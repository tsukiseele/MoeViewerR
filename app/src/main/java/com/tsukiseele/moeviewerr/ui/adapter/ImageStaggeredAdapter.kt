package com.tsukiseele.moeviewerr.ui.adapter

import android.content.Context
import android.graphics.Bitmap
import android.util.SparseIntArray
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.util.containsKey
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.model.Image
import com.tsukiseele.moeviewerr.libraries.BaseAdapter
import com.tsukiseele.moeviewerr.libraries.BaseViewHolder
import com.tsukiseele.moeviewerr.utils.DensityUtil
import com.tsukiseele.moeviewerr.utils.Util

class ImageStaggeredAdapter(
    context: Context,
    images: MutableList<Image>,
    private val column: Int
) : BaseAdapter<Image>(context, images, R.layout.item_image_staggered) {
    private val bitmapHeightData = SparseIntArray()

    override fun convert(context: Context, holder: BaseViewHolder, position: Int) {
        val data = list!![position]
        val ivCover = holder.getView<ImageView>(R.id.itemListImageStaggeredLayout_ImageView)
        val tvTitle = holder.getView<TextView>(R.id.itemListImageStaggeredLayoutScroll_TextView)
        tvTitle.text = data.title
        tvTitle.isSelected = true
        if (data.coverUrl.isNullOrBlank()) {
            ivCover.scaleType = ImageView.ScaleType.CENTER
            ivCover.setImageResource(R.drawable.ic_image_off_white)
            return
        }
        // 集合中不存在高度信息，则重加载并保存到集合
        if (!bitmapHeightData.containsKey(position)) {
            // 封面URL
            val url = data.coverUrl!!
            // 写入占位图
            ivCover.scaleType = ImageView.ScaleType.CENTER
            ivCover.setImageResource(R.drawable.ic_image_white)
            // 占位图高度
            var params = ivCover.layoutParams
            params.height = (DensityUtil.getScreenWidth(context) / column * 1.33).toInt()
            // 导入请求头
            val glideUrl = Util.buildGlideUrl(url, data.crawler!!.headers)

            // 请求图片
            Glide.with(context)
                .asBitmap()
                .load(glideUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .error(R.drawable.ic_image_off_white)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(bitmap: Bitmap, p2: Transition<in Bitmap>?) {
                        if (!bitmap.isRecycled) {
                            // 调整高度并重布局
                            val viewW = (DensityUtil.getScreenWidth(context) / column).toFloat()
                            val viewH = viewW / bitmap.width * bitmap.height
                            params = ivCover.layoutParams
                            params.height = viewH.toInt()
                            // 更新图片并启用动画效果
                            val anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
//                            val anim = AnimationUtils.loadAnimation(context, R.anim.anim_narrow)
                            ivCover.setImageBitmap(bitmap)
                            ivCover.scaleType = ImageView.ScaleType.CENTER_CROP
                            ivCover.animation = anim
                            anim.start()
                            // 缓存高度信息，缓存之后不会再次重绘此项，防止错乱
                            bitmapHeightData.put(position, params.height)
                        }
                    }
                })
        }
    }

    /**
     * 由于瀑布流的每个Item都不同，为防止图片错乱
     * 需要重写该方法使其返回一个唯一值，这里使用Item索引
     *
     */
    override fun getItemViewType(position: Int): Int {
        return position
    }

    /**
     * 更新数据的时候要清除相应的高度数据
     * 让Item可以被重绘
     */
    override fun clear() {
        super.clear()
        bitmapHeightData.clear()
    }

    /**
     * 更新数据的时候要清除相应的高度数据
     * 让Item可以被重绘
     */
    override fun updateDataSet(datas: List<Image>) {
        super.updateDataSet(datas)
        bitmapHeightData.clear()
    }

    companion object {
        val TYPE_FLOW_2_COL = 10
        val TYPE_FLOW_3_COL = 11
    }
}
