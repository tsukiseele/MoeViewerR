package com.tsukiseele.moeviewerr

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.tsukiseele.sakurawler.utils.IOUtil.MB

@GlideModule
class MoeAppGlideModule : AppGlideModule() {

    internal var diskSize = 64 * MB
    internal var memorySize = Runtime.getRuntime().maxMemory().toInt() / 8  // 取1/8最大内存作为最大缓存

    override fun registerComponents(p1: Context, p2: Glide, p3: Registry) {

    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // 自定义内存和图片池大小
        builder.setMemoryCache(LruResourceCache(memorySize.toLong()))
        builder.setBitmapPool(LruBitmapPool(memorySize.toLong()))
        /*
        // 定义缓存大小和位置
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskSize.toLong()))  //内存中
        builder.setDiskCache(ExternalCacheDiskCacheFactory(context, "cache", diskSize.toInt())) //sd卡中
        // 默认内存和图片池大小

        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize(); // 默认内存大小
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize(); // 默认图片池大小
        builder.setMemoryCache(new LruResourceCache(defaultMemoryCacheSize)); // 该两句无需设置，是默认的
        builder.setBitmapPool(new LruBitmapPool(defaultBitmapPoolSize));

        // 定义图片格式
        // builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);
        // builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565); // 默认
        */
    }
}
