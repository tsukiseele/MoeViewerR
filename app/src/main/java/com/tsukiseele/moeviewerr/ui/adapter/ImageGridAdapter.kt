package com.tsukiseele.moeviewerr.ui.adapter

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.model.Image
import com.tsukiseele.moeviewerr.libraries.BaseAdapter
import com.tsukiseele.moeviewerr.libraries.BaseViewHolder
import com.tsukiseele.moeviewerr.utils.Util

class ImageGridAdapter(context: Context, imageList: MutableList<Image>) :
    BaseAdapter<Image>(context, imageList, R.layout.item_image_grid) {

    override fun convert(context: Context, holder: BaseViewHolder, position: Int) {
        val image = list!![position]

        val tvTitle = holder.getView<TextView>(R.id.itemListImageGridLayoutTitle_TextView)
        val ivCover = holder.getView<ImageView>(R.id.itemListImageGridLayoutCover_ImageView)
        tvTitle.text = image.title
        tvTitle.isSelected = true

        if (image.coverUrl.isNullOrBlank()) {
            ivCover.setImageResource(R.drawable.ic_image_off_white)
        } else {
            // 导入请求头
            val url = Util.buildGlideUrl(image.coverUrl!!, image.crawler?.headers)

            Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .transition(DrawableTransitionOptions().transition(R.anim.anim_fade_in))
                .apply(
                    RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_image_white)
                        .error(R.drawable.ic_image_off_white)
                )
                .into(ivCover)
        }
    }

    companion object {
        val TYPE_GRID_3_COL = 20
    }
}
