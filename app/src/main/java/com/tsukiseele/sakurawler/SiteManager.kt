package com.tsukiseele.sakurawler

import com.tsukiseele.sakurawler.model.Site
import com.tsukiseele.sakurawler.utils.IOUtil
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * @author TsukiSeele
 * @date 2020.02.18
 * @remark 规则管理器，用于管理加载的Site对象
 */
object SiteManager {
    private const val SITE_FILE_SUFFIX = ".json"
    private const val SITE_PACK_SUFFIX = ".zip"

    fun getErrorMessage(): Map<String, Exception> {
        return errorMessage
    }

    interface OnErrorListener {
        fun onError(errorMessage: Map<String, Exception>?)
    }

    // 站点列表组
    private val siteMap: MutableMap<String, MutableList<Site>> =
        HashMap()
    // 保存路径映射
    private val sitePaths: MutableMap<Int, File?> = HashMap()
    // 保存解析失败的错误规则的异常信息
    private val errorMessage: MutableMap<String, Exception> =
        HashMap()
    var onErrorListener: OnErrorListener? = null
    fun getSiteMap(): Map<String, MutableList<Site>> {
        return siteMap
    }
    /**
     * 扫描并载入指定目录下规则以及规则包，并监听异常信息
     * @param dir 目录
     * @param onErrorListener 异常回调
     */
    @JvmOverloads
    fun loadSites(dir: File, onErrorListener: OnErrorListener? = null) {
        SiteManager.onErrorListener = onErrorListener
        addSitesToMap(
            siteMap,
            loadSiteFromFile(dir)
        )
    }

    fun reloadSites(dir: File) {
        siteMap.clear()
        loadSites(dir)
    }

    /*
	 * 用于将规则分组
	 *
	 *
	 */
    private fun addSitesToMap(
        siteMap: MutableMap<String, MutableList<Site>>,
        siteList: List<Site>
    ) { // 生成映射
        for (site in siteList) addSiteToMap(siteMap, site)
        // 按标题字典排序
        for (sites in siteMap.values) {
            Collections.sort(sites) { a, b -> a.title!!.compareTo(b.title!!) }
        }
    }

    // 读取外部并将规则转化为对象

    private fun loadSiteFromFile(dir: File): List<Site> { // 获取规则文件
        val files = IOUtil.scanDirectory(
            dir,
            SITE_FILE_SUFFIX,
            SITE_PACK_SUFFIX
        )
        // 读取规则
        val sites: MutableList<Site> = ArrayList()
        for (file in files) {
            if (file.name.toLowerCase().endsWith(SITE_PACK_SUFFIX)) {
                try {
                    sites.addAll(readZipSites(file))
                } catch (e: IOException) {
                    errorMessage["[" + file.name + "]"] = e
                }
            } else { // 解析规则
                var site: Site? = null
                try {
                    val data: String = IOUtil.readText(file.absolutePath)
                    // 将JSON转换为对象并添加到列表
                    site = Site.Companion.fromJSON(data)
                } catch (e: Exception) {
                    errorMessage["[" + file.name + "]"] = e
                }
                if (site != null) { // 写入规则路径
                    sitePaths[site.hashCode()] = file
                    // 加入规则组
                    sites.add(site)
                }
            }
        }
        if (errorMessage.size > 0 && onErrorListener != null) onErrorListener!!.onError(
            errorMessage
        )
        return sites
    }

    @Throws(IOException::class)
    fun readZipSites(zip: File?): List<Site> {
        if (zip == null || !zip.exists()) return ArrayList(0)
        val sites: MutableList<Site> = ArrayList()
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(zip)
            val entries: Enumeration<*> = zipFile.entries()
            var entry: ZipEntry
            while (entries.hasMoreElements()) {
                entry = entries.nextElement() as ZipEntry
                if (entry.isDirectory || !entry.name.toLowerCase().endsWith(SITE_FILE_SUFFIX)) continue
                try {
                    val bytes =
                        IOUtil.readByteArray(zipFile.getInputStream(entry))
                    sites.add(Site.fromJSON(String(bytes!!, Charset.forName(IOUtil.DEFAULT_CHARSET))))
                } catch (e: Exception) {
                    errorMessage["[" + IOUtil.getUrlFilename(zip.name) + ": " + entry.name + "]"] = e
                }
            }
        } finally {
            IOUtil.close(zipFile)
        }
        return sites
    }

    fun addSiteToMap(
        sites: MutableMap<String, MutableList<Site>>,
        site: Site
    ) {
        var type = site.type
        if (type.isNullOrEmpty()) type = "unknown"
        val list: MutableList<Site>?
        // 如果组不存在，则新建
        if (sites.containsKey(type)) {
            list = sites[type]
        } else {
            list = ArrayList()
            sites[type] = list
        }
        list!!.add(site)
    }

    fun addSite(site: Site) {
        addSiteToMap(
            siteMap,
            site
        )
    }

    fun getSiteList(key: String?): List<Site>? {
        return if (siteMap.containsKey(key)) siteMap[key] else null
    }

    fun findSiteByTitle(title: String): Site? {
        if (title.isEmpty()) return null
        siteMap.forEach {
            it.value.forEach {
                if (it.title!!.toLowerCase().contains(title.toLowerCase())) return it
            }
        }
//        for ((_, value) in siteMap) {
//            for (site in value) {
//                if (site.title!!.toLowerCase().contains(title.toLowerCase())) return site
//            }
//        }
        return null
    }

    fun getSitePaths(): Map<Int, File?> {
        return sitePaths
    }
}