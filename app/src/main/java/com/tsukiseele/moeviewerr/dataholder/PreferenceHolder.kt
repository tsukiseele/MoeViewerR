package com.tsukiseele.moeviewerr.dataholder

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.tsukiseele.moeviewerr.app.App

@SuppressLint("StaticFieldLeak")
object PreferenceHolder {
    private val context = App.context

    const val FILE_NAME = "preference"

    const val KEY_LISTTYPE = "listType"
    const val KEY_LASTOPEN_SITE = "lastOpenSite"
    const val KEY_FIRST_START = "firstStart"

    val preference = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = preference.edit()

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preference.getBoolean(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return preference.getInt(key, defaultValue)
    }

    fun getString(key: String, defaultValue: String): String? {
        return preference.getString(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return preference.getFloat(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return preference.getLong(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.commit()
    }

    fun putInt(key: String, value: Int) {
        editor.putInt(key, value)
        editor.commit()
    }

    fun putString(key: String, value: String) {
        editor.putString(key, value)
        editor.commit()
    }

    fun putFloat(key: String, value: Float) {
        editor.putFloat(key, value)
        editor.commit()
    }

    fun putLong(key: String, value: Long) {
        editor.putLong(key, value)
        editor.commit()
    }
}
