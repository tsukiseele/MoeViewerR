package com.tsukiseele.sakurawler.core

import java.util.regex.Pattern

object Const {
    val PATTERN_SELECTOR = Pattern.compile("(?<=\\$\\().*(?=\\)\\.)")
    val PATTERN_FUN = Pattern.compile("(?<=\\.)(text|html|attr)")
    val PATTERN_ATTR = Pattern.compile("(?<=attr\\().*?(?=\\))")
    val PATTERN_CONTENT_PAGE =
        Pattern.compile("(?<=\\{page:)(-?\\d*)?,?(-?\\d*)?(?=\\})")
    val PATTERN_CONTENT_KEYWORD = Pattern.compile("(?<=\\{keyword:).*(?=\\})")
    const val REGEX_PLACEHOLDER_PAGE = "\\{page:.*?\\}"
    const val REGEX_PLACEHOLDER_KEYWORD = "\\{keywords:.*?\\}"
}