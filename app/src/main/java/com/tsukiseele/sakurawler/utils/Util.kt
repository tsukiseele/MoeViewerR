package com.tsukiseele.sakurawler.utils

object Util {
    // 利用反射生成一个包含对象所有字段信息的字符串
    fun toString(obj: Any): String {
        val sb = StringBuilder()
        sb.append('[')
        try {
            var type: Class<*> = obj.javaClass
            while (type != Any::class.java) {
                val fields = type.declaredFields
                for (field in fields) {
                    field.isAccessible = true
                    sb.append(String.format("%s = %s, ", field.name, field[obj]))
                }
                type = type.superclass!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sb.substring(0, sb.length - 2) + ']'
    }

    /**
     * Determine whether the Class identified by the supplied name is present and can be loaded.
     * 判断由提供的类名(类的全限定名)标识的类是否存在并可以加载
     * 如果类或其中一个依赖关系不存在或无法加载，则返回false
     * @param className 要检查的类的名称
     * 可以是 null, 表明使用默认的类加载器
     * @return 指定的类是否存在
     */
    fun isPresent(className: String): Boolean {
        try {
            Class.forName(className)
            return true
        }
        catch (ex: Throwable) {
            // Class or one of its dependencies is not present...
            return false
        }
    }
}