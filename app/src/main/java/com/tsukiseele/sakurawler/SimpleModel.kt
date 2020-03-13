package com.tsukiseele.sakurawler

import com.tsukiseele.sakurawler.core.ModelMap

/**
 * @author TsukiSeele
 * @date 2020.02.18
 * @remark ModelMap的简单实现类
 */
class SimpleModel : ModelMap() {
    override val attrs: MutableMap<String?, String?> = HashMap()
    override val catalogUrl: String?
        get() = attrs.get("catalogUrl")
    override val extraUrl: String?
        get() = attrs.get("extraUrl")
}