package com.tsukiseele.sakurawler.core

import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.sakurawler.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import kotlin.math.log


/**
 * @author TsukiSeele
 * @date 2020.02.18
 * @remark 解析文档数据
 */
class HtmlParser<T : Model>(
    private val baseCrawler: BaseCrawler,
    private val type: Class<T>
) {

    interface MetaDataParseCallback<T : Model?> {
        fun onCatalogSuccess(datas: Catalog<T>?)
        fun onItemSuccess(data: T)
    }

    interface CatalogLoadCallback<T : Model> {
        fun onPageLoaded(catalog: Catalog<T>, currentDatas: List<T>, page: Int)
        fun onSuccessful(catalog: Catalog<T>)
        fun onFailed(catalog: Catalog<T>, e: Throwable)
    }

    interface ParsedCallback<T : Model> {
        fun onParsed(data: T)
    }
    /**
     * 深度解析规则，返回数据组集合，该操作极其耗时
     *
     */
    /*
    @Throws(IOException::class)
    fun parseAll(callback: MetaDataParseCallback<*>): Gallery<*> {
        val dataSet =
            Gallery<Catalog<T?>>(
                ArrayList<Catalog<T>>(),
                baseCrawler
            )
        val datas = parseGallery()
        if (datas != null && !datas.isEmpty()) { //具有目录结构
            var isCatalog = false
            for (data in datas) {
                if (data!!.hasCatalog()) {
                    val catalog = parseCatalog(data)
                    for (dat in catalog) {
                        if (dat!!.hasExtra()) parseFillExtra(dat)
                    }
                    callback.onCatalogSuccess(catalog)
                    dataSet.add(catalog)
                    isCatalog = true
                } else {
                    callback.onItemSuccess(data)
                }
            }
            if (!isCatalog) {
                return datas
            }
        }
        return dataSet
    }*/

    /**
     * 解析画廊内容
     */
    @Throws(IOException::class)
    fun parseGallery(): Gallery<T> {
        val html = baseCrawler.execute()
        val gallerySelectors =
            baseCrawler.section!!.gallerySelectors
        //		LogUtil.i(DocumentParser.class.getCanonicalName(), gallerySelectors.toString());
        val datas: List<T> = parseHtmlDocument(Jsoup.parse(html), gallerySelectors, type)
        return Gallery(
            datas,
            baseCrawler.section,
            baseCrawler.mode.pageCode,
            baseCrawler.mode.extraKey
        )
    }

    /**
     * 解析指定页画廊内容
     *
     */
    @Throws(IOException::class)
    fun parseGallery(pageCode: Int): Gallery<T> {
        baseCrawler.mode.pageCode = pageCode
        return parseGallery()
    }

    /**
     * 解析目录内容，包含所有页面
     * 会阻塞线程
     */
    fun parseCatalog(map: T, callback: CatalogLoadCallback<T>?): Catalog<T> {
        val section = baseCrawler.section
        val catalog = Catalog(section, map)
        val catalogSelectors = section!!.catalogSelectors

        try {
            val matcher = Const.PATTERN_CONTENT_PAGE.matcher(map!!.catalogUrl)
            if (matcher.find()) {
                // 匹配正则代表存在多页

                var datas: List<T>?
                var pageCount = 0
                val parent = catalog.parent
                // flag保存上一页的数据，用于检测是否重复爬取，防止死循环
                var flag: T? = null
                try {
                    while (true) {
                        val url =
                            BaseCrawler.replacePageCode(map.catalogUrl, catalog.pageCode++)
                        val html = baseCrawler.request(url)

                        datas = parseHtmlDocument(Jsoup.parse(html), catalogSelectors, type)
                        // 数据集为空直接返回
                        if (datas.isNotEmpty()) {
                            parent.fillTo(datas[0])
                            if (!datas[0].equals(flag)) {
                                parent.fillToAll(datas)
                                catalog.addAll(datas)
                                flag = datas[0]

                                if (callback != null)
                                    callback.onPageLoaded(catalog, datas, ++pageCount)
                            } else
                                break
                        } else
                            break
                    }
                    // 注意：此处可以进一步优化，这里是为了直接忽略多页解析所抛出的异常，直接返回Successful，这并不严谨
                } catch (e: Exception) {
                    callback?.onSuccessful(catalog)
                }

            } else {
                // 不存在多页
                val html = baseCrawler.request(map.catalogUrl!!)
                catalog.addAll(parseHtmlDocument(Jsoup.parse(html), catalogSelectors, type))
                catalog.parent!!.fillToAll(catalog)
            }
            /*for (T data : catalog)
			 data.setCatalogParsed(true);*/
            // 填充所有内容

            callback?.onSuccessful(catalog)
        } catch (e: IOException) {
            callback?.onFailed(catalog, e)
        }

        return catalog
    }

    /**
     * 解析目录内容，包含所有页面
     *
     *
     */
    @Throws(IOException::class)
    fun parseCatalog(map: T): Catalog<T> {
        val section = baseCrawler.section
        val catalog = Catalog(section, map)
        val catalogSelectors = section!!.catalogSelectors
        val matcher = Const.PATTERN_CONTENT_PAGE.matcher(map?.catalogUrl!!)
        if (matcher.find()) { // 匹配正则代表存在多页
            // flag保存上一页的数据，用于检测是否重复爬取，防止死循环
            var flag: T? = null
            var datas: List<T>
            while (true) {
                val url: String = BaseCrawler.replacePageCode(map.catalogUrl!!, catalog.pageCode++)
                val html = baseCrawler.request(url)
                datas = parseHtmlDocument(Jsoup.parse(html), catalogSelectors, type)
                // 数据集为空直接返回
                flag = if (datas.size > 0) {
                    if (flag == null || datas[0].hashCode() != flag.hashCode()) {
                        catalog.addAll(datas)
                        datas[0]
                    } else break
                } else break
            }
        } else { // 不存在多页
            val html = baseCrawler.request(map.catalogUrl!!)
            catalog.addAll(parseHtmlDocument(Jsoup.parse(html), catalogSelectors, type))
        }
        // 填充所有内容
        catalog.parent!!.fillToAll(catalog)
        return catalog
    }

    /**
     * 解析额外规则
     *
     */
    @Throws(IOException::class)
    fun parseFillExtra(map: T) {
        if (map.hasExtra()) {
            val extraSelectors =
                baseCrawler.section!!.extraSelectors
            val html = baseCrawler.request(map.extraUrl!!)
            // 填充数据
            val metadata = parseHtmlDocument(Jsoup.parse(html), extraSelectors, type)[0]
            metadata.coverTo(map)
        }
    }

    fun parseExtraAsync(map: T, callback: ParsedCallback<T>) {
        Thread {
            try {
                parseFillExtra(map)
                callback.onParsed(map)
            } catch (e: IOException) {

            }
        }.start()
    }

    /**
     * @parem doc 需要解析的Html文档
     * @param selectors 选择器组
     * @param type 生成的数据类型
     *
     * @return type类型的数据容器
     */
    private fun <T : Model?> parseHtmlDocument(
        doc: Document,
        selectors: Map<String, Selector?>?,
        type: Class<T>
    ): List<T> {
        val datasMap: MutableMap<String?, Array<String?>> = hashMapOf()
        val commonDatas: MutableMap<String?, String> = hashMapOf()
//        println("Selectors: " + selectors)
        var length = 0
        for (key in selectors!!.keys) {
            val selector = selectors[key]
            val datas = queryHtmlElement(doc, selector)
            if (selector?.isCommon ?: false) {
                val value = StringBuilder().apply {
                    datas.forEach {
                        append(it).append(",")
                    }
                }.toString()
                commonDatas.put(key, value)
                continue
            }
//            println("data: " + Arrays.toString(strings))
            datasMap[key] = datas
            // 更新数据列表的最大长度
            if (length < datas.size) length = datas.size
        }
        // 构造对象组
        val dataGroupKeySet: Set<String?> = datasMap.keys
        val models: MutableList<T> = ArrayList()
        try {
            for (i in 0 until length) {
                val model = type.newInstance()
                if (model is ModelMap) {
                    val modelMap = (model as ModelMap).attrs
                    for (key in dataGroupKeySet) {
                        val datas: Array<String?>? = datasMap[key]
                        if (i < datas!!.size) modelMap[key] = datas[i]
                    }
                    // 添加共有数据
                    modelMap.putAll(commonDatas)
                } else {
                    for (key in dataGroupKeySet) {
                        val datas: Array<String?>? = datasMap[key]
                        try {
                            val field = type.getDeclaredField(key!!)
                            field.isAccessible = true
                            if (i < datas!!.size) field[model] = datas[i]
                        } catch (e: NoSuchFieldException) {
                            continue
                        }
                    }
                    // 添加共有数据
                    for ((key, value) in commonDatas) {
                        val field = type.getDeclaredField(key!!)
                        field.isAccessible = true
                        field[model] = value
                    }
                }
                model?.crawler = baseCrawler
                models.add(model)
            }
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return models
    }

    companion object {
        /**
         * @param doc HTML文档
         * @param selector 选择器
         *
         * @return String[] 选择到的内容
         */
        private fun queryHtmlElement(
            doc: Document,
            selector: Selector?
        ): Array<String?> {
            if (selector == null) throw NullPointerException("The Selector can not be null")
            // 数据容器
            val datas: MutableList<String?> = ArrayList()
            // 利用选择器获取
            if (!selector.selector.isNullOrBlank()) {
                selector.init()
                val es = doc.select(selector.selector)
                for (e in es) { //				LogUtil.i("parseHtmlElement", e.toString());
                    var content: String? = null
                    content = if (selector.func != null) {
                        when (selector.func) {
                            "attr" -> e.attr(selector.attr)
                            "html" -> e.toString()
                            "text" -> e.text()
                            else -> e.toString()
                        }
                    } else {
                        e.toString()
                    }
                    content = replaceContent(
                        content,
                        selector.capture,
                        selector.replacement
                    )
                    if (!content.isNullOrBlank()) datas.add(content)
                }
                // 利用正则获取
            } else if (!selector.regex.isNullOrBlank()) {
                val pattern =
                    Pattern.compile(selector.regex, Pattern.DOTALL)
                val matcher = pattern.matcher(doc.toString())
                while (matcher.find()) {
                    val item = matcher.group(1)
                    val data = replaceContent(
                        item,
                        selector.capture,
                        selector.replacement
                    )
                    if (!data.isBlank()) datas.add(data)
                }
            } else {
                throw NullPointerException("The value of the selector cannot be empty.")
            }
            return datas.toTypedArray()
        }

        /**
         * @param content 待处理文本
         * @param capture 截取正则式
         * @param replacement 替换式，使用"$n"来作为匹配组的占位符
         *
         * @remark
         * 示例：
         *      content: /g/1546778/81f8c9f6d8/?inline_set=ts_l
         *      capture: [^\?]+
         *      replacement: https://exhentai.org$0
         *
         *      return： https://exhentai.org/g/1546778/81f8c9f6d8/
         *
         * 若content，capture，replacement均为null或空串，返回空串
         * 若content为null或空串，返回replacement
         * 若capture，replacement为null或空串，返回content
         * 若capture匹配失败，返回content
         * 若replacement不存在占位符，则将context的所有匹配项替换为replacement，
         *      同String.replace(Regex, String)
         */
        fun replaceContent(
            content: String?,
            capture: String?,
            replacement: String?
        ): String {
            if (content.isNullOrEmpty())
                return replacement ?: ""
            if (capture.isNullOrEmpty() || replacement.isNullOrEmpty())
                return content
            // 匹配组占位符正则
            val REGEX_GROUP_PLACEHOLDER = """(?<=\$)\d+""".toRegex()
            // 未匹配成功即返回
            val captureResult =
                capture.toRegex().find(content)?.groupValues
                    ?: return content
            // 替换匹配组
            var result: String = replacement
            // 无占位符则执行全文替换
            REGEX_GROUP_PLACEHOLDER.findAll(replacement).also {
                if (it.count() == 0)
                    return content.replace(capture.toRegex(), replacement)
            // 否则将占位符替换为匹配内容
            }.forEach { matchResult ->
                matchResult.value.toInt().also {
                    result = result.replace("\$${it}", captureResult.getOrNull(it) ?: "")
                }
            }
            return result
        }
    }
}