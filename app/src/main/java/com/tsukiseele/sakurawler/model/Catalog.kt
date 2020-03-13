package com.tsukiseele.sakurawler.model

import com.tsukiseele.sakurawler.core.Model
import java.io.Serializable
import java.util.*

// 保存目录数据和状态
/**
 * 子容器，存放数据条目
 * @param <T : Model?>
 */
class Catalog<T : Model?> : ArrayList<T>, Serializable {
    // 源数据
    var parent: T
    // 源Section
    var section: Section?
    // 目录当前页码
    var pageCode = 0

    constructor(
        datas: List<T>?,
        section: Section?,
        parent: T
    ) : super(datas ?: mutableListOf()) {
        this.section = section
        this.parent = parent
    }

    constructor(section: Section?, parent: T) : super() {
        this.section = section
        this.parent = parent
    }

    val isSingle: Boolean
        get() = size == 1
}