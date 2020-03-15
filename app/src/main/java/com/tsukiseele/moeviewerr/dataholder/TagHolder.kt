package com.tsukiseele.moeviewerr.dataholder

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.model.Tag
import java.io.File

import java.io.IOException
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

class TagHolder private constructor() {
    var tags: MutableList<Tag> = arrayListOf()
    private set

    var sortMode = MODE_SORT_TAG_TOP

    val allStringTag: Array<String>
        get() {

            val tags = Array(tags.size, { "" })

            for (i in this.tags.indices) {
                tags[i] = this.tags[i].tag ?: continue
            }
            return tags
        }

    init {
        loadTags()
        if (tags == null)
            tags = ArrayList()
    }

    fun getTag(index: Int): Tag {
        return tags!![index]
    }

    fun addTag(value: Tag) {
        for (label in tags) {
            if (label.tag == value.tag)
                return
        }
        tags.add(value)
        sort(sortMode)
    }

    private fun sort(mode: Int) {
        Collections.sort(tags, Comparator { a, b ->
            when (mode) {
                MODE_SORT_TAG_TOP -> return@Comparator a.tag!!.compareTo(b.tag!!)
                MODE_SORT_TAG_BOTTOM -> return@Comparator -a.tag!!.compareTo(b.tag!!)
            }
            0
        })
    }

    fun saveTags() {
        try {
            TAG_DATA_FILE.writeText(Gson().toJson(tags))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadTags() {
        if (!TAG_DATA_FILE.exists())
            return
        try {
            val json = TAG_DATA_FILE.readText()
            tags = Gson().fromJson<MutableList<Tag>>(json,
                object : TypeToken<MutableList<Tag>>() {}.type)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }



    companion object {
        val MODE_SORT_TAG_TOP = 10
        val MODE_SORT_TAG_BOTTOM = 11
        val TAG_DATA_FILE = File(Config.FILE_TAG)

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
