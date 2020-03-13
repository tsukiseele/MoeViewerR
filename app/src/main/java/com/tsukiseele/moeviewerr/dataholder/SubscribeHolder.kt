package com.tsukiseele.moeviewerr.dataholder

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.model.Subscribe
import com.tsukiseele.moeviewerr.utils.IOUtil
import java.io.File
import java.lang.Exception

class SubscribeHolder private constructor() : ArrayList<Subscribe>() {

    companion object {
        private var instance : SubscribeHolder? = null
            get() {
                if (field == null)
                    field = readConfig()
                return field
            }
        @Synchronized
        fun get(): SubscribeHolder {
            return instance!!
        }

        private fun readConfig(): SubscribeHolder {
            try {
                return Gson().fromJson<SubscribeHolder>(File(Config.FILE_SUBSCRIBE).readText(),
                    object : TypeToken<SubscribeHolder>() {}.type)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return SubscribeHolder()
        }

        fun saveConfig(): Boolean {
            try {
                IOUtil.writeText(Config.FILE_SUBSCRIBE, Gson().toJson(instance))
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }
    }
}
