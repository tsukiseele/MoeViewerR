package com.tsukiseele.sakurawler.model

import com.tsukiseele.sakurawler.utils.Util
import java.io.Serializable

/**
 * 定义网站各模块的选择器
 *
 */
class Section : Serializable {
    var indexUrl: String? = null
    val reuse: String? = null
    var gallerySelectors: Map<String, Selector?>? = null
    var catalogSelectors: Map<String, Selector?>? = null
    var extraSelectors: Map<String, Selector?>? = null

    fun reuse(section: Section?) {
        gallerySelectors = section!!.gallerySelectors
        catalogSelectors = section.catalogSelectors
        extraSelectors = section.extraSelectors
    }

    override fun toString(): String {
        return Util.toString(this)
    }
}