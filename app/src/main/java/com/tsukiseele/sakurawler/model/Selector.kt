package com.tsukiseele.sakurawler.model

import com.tsukiseele.sakurawler.core.Const
import com.tsukiseele.sakurawler.utils.RegexUtil
import com.tsukiseele.sakurawler.utils.Util
import java.io.Serializable

/**
 * 定义CSS选择器内容
 *
 */
class Selector : Serializable {
    // 选择器
    var selector: String? = null
    // 选择器方法
    var func: String? = null
    // 选择器参数
    var attr: String? = null
    // 匹配正则式，该值存在时选择器将会失效
    var regex: String? = null
    // 截取正则式
    var capture: String? = null
    // 替换式
    var replacement: String? = null
    // 是否应用到所有
    var isCommon: Boolean? = null

    /**
     * 初始化，将selector拆解为selector, func, attr三部分
     */
    fun init() {
        if (func.isNullOrBlank() && regex.isNullOrBlank()) {
            var selector: String? = null
            var func: String? = null
            var attr: String? = null
            // 提取选择器
            selector = RegexUtil.matchesText(this.selector, Const.PATTERN_SELECTOR)
            // 提取选择方法
            val matcher =
                Const.PATTERN_FUN.matcher(this.selector)
            if (matcher.find()) {
                when (matcher.group().trim { it <= ' ' }) {
                    "attr" -> {
                        func = "attr"
                        attr = RegexUtil.matchesText(this.selector, Const.PATTERN_ATTR)
                    }
                    "html" -> func = "html"
                    "text" -> func = "text"
                }
            }
            this.selector = selector
            this.func = func
            this.attr = attr
        }
    }

    override fun toString(): String {
        return Util.toString(this)
    }
}