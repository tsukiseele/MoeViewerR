package com.tsukiseele.moeviewerr.dataholder

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.model.Tag
import java.io.File
import java.io.IOException

class TagHolder private constructor() {
    var tags = LinkedHashMap<String, Tag>()
        private set

    val allStringTag: Array<String>
        get() {
            return tags.keys.toTypedArray()
        }

    init {
        load()
    }

    fun addAll(from: Map<out String, Tag>) {
        tags.putAll(from)
        sort()
    }

    fun add(value: Tag) {
        tags.put(value.tag, value)
        sort()
    }

    private fun sort() {
        tags = tags.toList().sortedBy { it.first }.toMap().toMutableMap() as LinkedHashMap<String, Tag>
    }

    fun save() {
        try {
            File(Config.FILE_TAG).writeText(Gson().toJson(tags))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun load() {
        if (!File(Config.FILE_TAG).exists())
            return
        try {
            val json = File(Config.FILE_TAG).readText()
            tags = Gson().fromJson<LinkedHashMap<String, Tag>>(json,
                object : TypeToken<LinkedHashMap<String, Tag>>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val MODE_SORT_TAG_TOP = 1
        val MODE_SORT_TAG_BOTTOM = 2

        private var tagHolder: TagHolder? = null

        val instance: TagHolder
            get() {
                if (tagHolder == null)
                    tagHolder = TagHolder()
                return tagHolder as TagHolder
            }

        fun toJson(): String{
            return Gson().toJson(instance.tags)
        }
    }
}
