package com.tsukiseele.moeviewerr.dataholder

import java.util.HashMap

object GlobalObjectHolder {
    const val STATE_TAGS_UPDATED = "tags_updated"

    private val objects = HashMap<String, Any>()

    fun put(key: String, obj: Any) {
        objects[key] = obj
    }

    fun remove(key: String): Any? {
        return if (objects.containsKey(key))
            objects.remove(key) else null
    }
}
