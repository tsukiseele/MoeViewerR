package com.tsukiseele.moeviewerr.model

import com.tsukiseele.sakurawler.core.Model
import com.tsukiseele.sakurawler.utils.Util
import java.io.Serializable

open class Image : Model(), Serializable {
    var title: String? = null
    var tags: String? = null
    override var catalogUrl: String? = null
    override var extraUrl: String? = null
    var coverUrl: String? = null
    var sampleUrl: String? = null
    var largerUrl: String? = null
    var originUrl: String? = null
    var datetime: String? = null

    fun getLowUrl(): String? {
        return when {
            !sampleUrl.isNullOrBlank() -> sampleUrl
            !largerUrl.isNullOrBlank() -> largerUrl
            else -> originUrl
        }
    }

    fun getHighUrl(): String? {
        return when {
            !originUrl.isNullOrBlank() -> originUrl
            !largerUrl.isNullOrBlank() -> largerUrl
            else -> sampleUrl
        }
    }

    override fun toString(): String {
        return Util.toString(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (title != other.title) return false
        if (tags != other.tags) return false
        if (catalogUrl != other.catalogUrl) return false
        if (extraUrl != other.extraUrl) return false
        if (coverUrl != other.coverUrl) return false
        if (sampleUrl != other.sampleUrl) return false
        if (largerUrl != other.largerUrl) return false
        if (originUrl != other.originUrl) return false
        if (datetime != other.datetime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (tags?.hashCode() ?: 0)
        result = 31 * result + (catalogUrl?.hashCode() ?: 0)
        result = 31 * result + (extraUrl?.hashCode() ?: 0)
        result = 31 * result + (coverUrl?.hashCode() ?: 0)
        result = 31 * result + (sampleUrl?.hashCode() ?: 0)
        result = 31 * result + (largerUrl?.hashCode() ?: 0)
        result = 31 * result + (originUrl?.hashCode() ?: 0)
        result = 31 * result + (datetime?.hashCode() ?: 0)
        return result
    }
}
