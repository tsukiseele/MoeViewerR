package com.tsukiseele.moeviewerr.utils

import java.io.ObjectOutputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ByteArrayInputStream
import org.json.JSONObject
import org.json.JSONException

object ObjectUtil {
    // 序列化拷贝对象
    fun <T> cloneObject(`object`: T): T? {
        var oos: ObjectOutputStream? = null
        var ois: ObjectInputStream? = null
        var newObject: T? = null
        try {
            // 序列化
            val baos = ByteArrayOutputStream()
            oos = ObjectOutputStream(baos)
            oos.writeObject(`object`)
            // 反序列化
            val bais = ByteArrayInputStream(baos.toByteArray())
            ois = ObjectInputStream(bais)
            newObject = ois.readObject() as T
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                oos!!.close()
                ois!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return newObject
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
    // 利用反射生成一个包含对象所有字段信息的映射
    fun toMap(obj: Any): MutableMap<String, String?> {
        var type: Class<*>? = obj.javaClass
        val map = mutableMapOf<String, String?>()
        try {
            while (type != Any::class.java) {
                val fields = type!!.declaredFields
                for (field in fields) {
                    field.isAccessible = true
                    map.put(field.name, field.get(obj)?.toString())
                }
                type = type.superclass
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }
    // 利用反射生成一个JSONObject对象
    @Throws(JSONException::class, IllegalAccessException::class, IllegalArgumentException::class)
    fun toJSONObject(`object`: Any): JSONObject {
        val json = JSONObject()
        var type: Class<*>? = `object`.javaClass
        while (type != Any::class.java) {
            val fields = type!!.declaredFields
            for (field in fields) {
                field.isAccessible = true
                val fieldName = field.name
                val obj = field.get(`object`)
                if (obj == null) {
                    json.put(fieldName, "")
                } else {
                    json.put(fieldName, obj)
                }
            }
            type = type.superclass
        }
        return json
    }
} 

