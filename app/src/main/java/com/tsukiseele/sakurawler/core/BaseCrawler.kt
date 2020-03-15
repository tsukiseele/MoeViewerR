package com.tsukiseele.sakurawler.core

import com.tsukiseele.sakurawler.model.Section
import com.tsukiseele.sakurawler.model.Site
import com.tsukiseele.sakurawler.utils.HttpUtil
import com.tsukiseele.sakurawler.utils.IOUtil.readText
import java.io.IOException
import java.io.Serializable
import java.util.*
import kotlin.random.Random

/**
 * @author TsukiSeele
 * @date 2020.02.18
 * @remark 抽象类，重写此类部分方法以自定义HTML加载方式与逻辑
 */
abstract class BaseCrawler : Serializable {
    var parser: HtmlParser<out Model>? = null
        private set
    abstract val mode: Mode
    abstract val site: Site

    /**
     * @param url 请求的URL
     * @return 请求到的HTML文档
     * @throws IOException
     *
     * @remark
     * 推荐重写此方法来提升请求网页的性能。
     * 也可以在此加载网页中的JavaScript从而抓取静态网页所获取不到的内容。
     *
     * 默认使用标准库的HttpURLConnection进行网页请求。
     */
    @Throws(IOException::class)
    open fun request(url: String): String {
        return readText(HttpUtil.requestStream(url, headers))
    }

    /**
     * @return HTML文档
     * @throws IOException
     *
     * @remark 解析器在解析Gallery时调用
     */
    @Throws(IOException::class)
    fun execute(): String {
        val section =
            section ?: throw NullPointerException("section not exists! key = " + mode.extraKey)
        if (section.indexUrl.isNullOrBlank())
            throw NullPointerException("\"indexUrl\" is null or blank! mode.type=${mode.type}")
        // 处理URL，替换里面的占位符和转义字符
        val url =
            encodeURL(replaceUrlPlaceholder(section.indexUrl, mode.pageCode, mode.keywords))
        return request(url)
    }

    /**
     * 获取当前Site的Section
     *
     * @return 当前Site的Section
     */
    val section: Section?
        get() = getCurrentSection(site, mode.type, mode.extraKey)

    /**
     * 构造解析器
     *
     * @param type 元数据Class对象
     * @param <T> 元数据类型
     * @return 关于元数据的数据解析器
     */
    open fun <T : Model> parseOf(type: Class<T>): HtmlParser<T> {
        return HtmlParser(this, type).also { parser = it }
    }

    /**
     * 获取当前Site的请求头
     *
     * @return 实际请求头
     */
    val headers: Map<String, String>
        get() {
            var requestHeaders =
                site.requestHeaders
            if (requestHeaders == null) requestHeaders = HashMap()
            if (isUseDefaultUserAgent) {
                requestHeaders["User-Agent"] = defaultUserAgent
            }
            return requestHeaders
        }

    /**
     * 替换所有占位符
     *
     * @param templateUrl 模板URL
     * @param pageCode 目标页码
     * @param keyword 关键字
     * @return 目标URL
     */
    private fun replaceUrlPlaceholder(
        templateUrl: String?,
        pageCode: Int,
        keywords: String?
    ): String {
        return replacePageCode(replaceKeywords(templateUrl, keywords), pageCode)
    }

    /**
     *
     * 保持爬取前传入的状态
     *
     * @param type Site的类型（图片，视频等等。。。）
     * @param pageCode 当前页码
     * @param keywords 当然关键字
     * @param extraKey 扩展规则键值
     * @param extraData 扩展规则数据
     */
    class Mode(
        var type: Int = MODE_HOME,
        var pageCode: Int = 1,
        var keywords: String? = null,
        var extraKey: String? = null,
        var extraData: String? = null
    ) : Serializable

    companion object {
        const val MODE_HOME = 0
        const val MODE_SEARCH = 1
        const val MODE_EXTRA = 2

        private var isUseDefaultUserAgent = true

        val USER_AGENTS = arrayOf(
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.109 Safari/537.36",
            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0;",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11",
            "Mozilla/5.0 (Windows NT 6.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1"
        )

        /**
         * 设置是否添加默认请求头(默认UA)，默认为true
         *
         * @param isUseDefaultUserAgent
         */
        fun setIsUseDefaultUserAgent(isUseDefaultUserAgent: Boolean) {
            Companion.isUseDefaultUserAgent = this.isUseDefaultUserAgent
        }

        /**
         * 生成随机的默认浏览器UA
         *
         * @return 随机生成的浏览器UA
         */
        val defaultUserAgent: String
            get() = USER_AGENTS[Random.nextInt(USER_AGENTS.size)]

        /**
         * 占位符用法：
         * {page:a} 表示页面从pageCode + a页开始加载
         * {page:a, b} 表示页面从(pageCode + a) * b页开始加载
         *
         * a 补正码，对pageCode给予一定的补正
         * b 步长，改变pageCode的间隔值
         *
         * @param templateUrl 模板url
         * @param pageCode 目标页码
         * @return 实际url
         */
        fun replacePageCode(templateUrl: String?, pageCode: Int): String { // 补正码
            var correct = 0
            // 步长，默认为1
            var pace = 1
            // 替换页码
            val pageMatcher =
                Const.PATTERN_CONTENT_PAGE.matcher(templateUrl)
            if (pageMatcher.find()) {
                val groupCount = pageMatcher.groupCount()
                val ints = intArrayOf(0, 1)
                for (i in 1..groupCount) {
                    val group = pageMatcher.group(i)
                    if (!group.isNullOrBlank()) ints[i - 1] = group.toInt()
                }
                correct = ints[0]
                pace = ints[1]
            }
            // 页码值 (当前页码 + 起始页码) * 修正码
            return templateUrl!!.replace(
                Const.REGEX_PLACEHOLDER_PAGE.toRegex(),
                ((pageCode + correct) * pace).toString()
            )
        }

        /**
         * {keywords:} 表示该位置会替换为搜索标签
         *
         * @param templateUrl 模板URL
         * @param keyword 目标关键字
         * @return 实际URL
         */
        fun replaceKeywords(
            templateUrl: String?,
            keywords: String?
        ): String {
            if (templateUrl.isNullOrBlank()) return ""
            return templateUrl.replace(Const.REGEX_PLACEHOLDER_KEYWORD.toRegex(),
                keywords ?: {
                // 关键字为空，则使用默认关键字
                Const.PATTERN_CONTENT_KEYWORD.matcher(templateUrl).let {
                    if (it.find()) it.group() else ""
                }
            }())
        }

        /**
         * 编码URL
         *
         */
        fun encodeURL(url: String): String {
//            var url = url
//            val ESCAPES = arrayOf(
//                arrayOf("&amp;", "&"),
//                arrayOf(" ", "%20")
//            )
//            for (escape in ESCAPES) url =
//                url.replace(escape[0].toRegex(), escape[1])
            return url
        }

        /**
         * 通过Site找到当前Rule
         *
         */
        fun getCurrentSection(site: Site, mode: Int, extraKey: String?): Section? {
            var section: Section? = null
            when (mode) {
                MODE_HOME -> section = site.homeSection
                MODE_SEARCH -> section = site.searchSection
                MODE_EXTRA -> if (site.extraSections != null && !site.extraSections!!.isEmpty()) section =
                    site.extraSections!![extraKey]
            }
            return section
        }
    }
}