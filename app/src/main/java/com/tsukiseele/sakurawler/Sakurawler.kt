package com.tsukiseele.sakurawler

import com.tsukiseele.sakurawler.core.BaseCrawler
import com.tsukiseele.sakurawler.model.Site
import com.tsukiseele.sakurawler.utils.OkHttpUtil
import com.tsukiseele.sakurawler.utils.Util
import java.lang.Class.forName

/**
 * @author TsukiSeele
 * @date 2020.02.18
 */
open class Sakurawler(override val site: Site, override val mode: Mode = Mode()) : BaseCrawler() {
    companion object {
        fun load(site: Site): Sakurawler {
            return Sakurawler(site)
        }
    }

    fun pagecode(pageCode: Int): Sakurawler {
        mode.pageCode = pageCode
        return this
    }

    fun home(): Sakurawler {
        mode.type = MODE_HOME
        return this
    }

    fun keywords(keywords: String?): Sakurawler {
        mode.type = if (keywords.isNullOrEmpty()) MODE_HOME else MODE_SEARCH
        mode.keywords = keywords
        return this
    }

    fun extra(extraKey: String?, extraData: String? = null): Sakurawler {
        mode.type = MODE_EXTRA
        mode.extraKey = extraKey
        mode.extraData = extraData
        return this
    }

    fun mode(mode: Mode): Sakurawler {
        this.mode.type = mode.type
        this.mode.pageCode = mode.pageCode
        this.mode.keywords = mode.keywords
        this.mode.extraKey = mode.extraKey
        this.mode.extraData = mode.extraData
        return this
    }

    override fun request(url: String): String {
        if (Util.isPresent("okhttp3.OkHttpClient")) {
            return OkHttpUtil.get(url, headers).body()?.string() ?: ""
        } else {
            return super.request(url)
        }
    }
}