package com.tsukiseele.moeviewerr.utils

object TextUtil {
    fun isEmpty(text: String?): Boolean {
        return null == text || 0 == text.trim { it <= ' ' }.length
    }

    fun nonEmpty(text: String?): Boolean {
        return null != text && 0 != text.trim { it <= ' ' }.length
    }

    // 利用反射生成一个包含对象所有字段信息的字符串
    fun toString(obj: Any): String {
        var type: Class<*>? = obj.javaClass
        val sb = StringBuilder()
        try {
            while (type != Any::class.java) {
                val fields = type!!.declaredFields
                for (field in fields) {
                    field.isAccessible = true
                    sb.append(field.name + " = " + field.get(obj) + ", ")
                }
                type = type.superclass
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return if (sb.length == 0) "" else sb.substring(0, sb.length - 2)
    }
} 
