package com.tsukiseele.sakurawler.utils

import java.util.regex.Pattern

object RegexUtil {
    fun matchesText(text: String?, pattern: Pattern?): String? {
        var matchText: String? = null
        val matcher = pattern!!.matcher(text)
        if (matcher.find()) matchText = matcher.group()
        return matchText
    }

    fun matchesText(text: String?, regex: String?): String? {
        return matchesText(text, Pattern.compile(regex))
    }
}