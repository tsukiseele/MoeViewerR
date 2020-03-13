package com.tsukiseele.sakurawler.core

/**
 * @author TsukiSeele
 * @date 2020.02.18
 * 所有用于映射抓取数据的Bean都必须继承该抽象类
 */
abstract class Model {
    @Transient
    var crawler: BaseCrawler? = null
        get() = field
        internal set(value) { field = value }
    abstract val catalogUrl: String?
    abstract val extraUrl: String?
    fun hasCatalog(): Boolean {
        return !catalogUrl.isNullOrBlank()
    }

    fun hasExtra(): Boolean {
        return !extraUrl.isNullOrBlank()
    }

    open fun fillTo(model: Model?) {
        model ?: return
        val fields = model.javaClass.declaredFields
        for (field in fields) {
            field.isAccessible = true
            try {
                if (field[model] == null) {
                    val obj = field[this]
                    if (obj != null) field[model] = obj
                }
            } catch (e: Exception) {}
        }
    }

    open fun fillToAll(models: List<Model?>?) {
        models ?: return
        for (model in models)
            fillTo(model)
    }

    open fun coverTo(model: Model?) {
        model ?: return
        val fields = model.javaClass.declaredFields
        for (field in fields) {
            field.isAccessible = true
            try {
                val obj = field[this]
                if (obj != null) field[model] = obj
            } catch (e: Exception) {}
        }
    }

    open fun coverToAll(models: List<Model?>?) {
        models ?: return
        for (model in models)
            coverTo(model)
    }
}