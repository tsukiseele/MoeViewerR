package com.tsukiseele.sakurawler.model

import java.io.Serializable
import java.util.*

/**
 * 存放数据条目与子容器的根容器
 * @param <T : Model?>
 */
class Gallery<T>(
    datas: List<T>?,
    // 源Section
    var section: Section?,
    // 当前页码
    var pageCode: Int,
    // 当前Tags
    var keywords: String?
) : ArrayList<T>(datas ?: mutableListOf()), Serializable