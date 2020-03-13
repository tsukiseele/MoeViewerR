package com.tsukiseele.sakurawler.model

import com.google.gson.Gson
import com.tsukiseele.sakurawler.utils.RegexUtil
import com.tsukiseele.sakurawler.utils.Util
import com.tsukiseele.sakurawler.utils.IOUtil
import java.io.File
import java.io.IOException
import java.io.Serializable

/**
 * 该类描述一个站点规则的所有配置参数与抓取方式
 * 与JSON形成映射
 */
class Site : Serializable {
    var title: String? = null
    var id = 0
    var version = 0
    var author: String? = null
    var remarks: String? = null
    var rating: String? = null
    var flag: String? = null
    var path: String? = null
    val type: String? = null
    var icon: String? = null
    private var json: String? = null
    var requestHeaders: MutableMap<String, String>? = null
    var homeSection: Section? = null
    var searchSection: Section? = null
    var extraSections: Map<String?, Section?>? = null
    private fun reuseSection(section: Section?) {
        if (section == null) return
        val reuse = section.reuse
        if (reuse.isNullOrBlank()) return
        if (reuse.contains("homeSection")) {
            section.reuse(homeSection)
        } else if (reuse.contains("searchSection")) {
            section.reuse(searchSection)
        } else if (reuse.contains("extraSections")) {
            val key = RegexUtil.matchesText(reuse, "(?<=\\().*?(?=\\))")
            section.reuse(extraSections!![key])
        }
    }

    fun hasFlag(flag: String?): Boolean {
        return if (this.flag.isNullOrBlank() || flag.isNullOrBlank())
            false else this.flag!!.contains(flag!!)
    }

    override fun toString(): String {
        return Util.toString(this)
    }

    fun toJson(): String? {
        return json
    }

    companion object {
        const val FLAG_STATE_OK = "flagStateOk"
        const val FLAG_STATE_INSTABLE = "flagStateInstable"
        const val FLAG_STATE_ABROAD = "flagStateAbroad"
        const val FLAG_LOAD_JS = "loadJs"
        const val FLAG_DEBUG = "debug"
        fun fromJSON(json: String?): Site {
            val gson = Gson()
            val site = gson.fromJson(json, Site::class.java)
            site.json = json
            site.reuseSection(site.homeSection)
            site.reuseSection(site.searchSection)
            if (site.extraSections != null) for ((_, value) in site.extraSections!!) site.reuseSection(
                value
            )
            return site
        }

        @Throws(IOException::class)
        fun fromJSON(jsonFile: File): Site {
            return fromJSON(IOUtil.readText(jsonFile.absolutePath))
        }
    }
}