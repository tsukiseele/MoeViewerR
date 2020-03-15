package com.tsukiseele.moeviewerr.ui.fragments

import android.content.Context
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.preference.SwitchPreference
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.App
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.dataholder.PreferenceHolder
import com.tsukiseele.moeviewerr.utils.GlideCacheUtil
import com.tsukiseele.sakurawler.SiteManager
import es.dmoral.toasty.Toasty
import java.io.File
import java.io.IOException

class SettingsFragment : PreferenceFragment() {

    private var onClearImageCachePreference: Preference? = null
    private var onDownloadPathPreference: Preference? = null
    private var onSafeModePreference: SwitchPreference? = null
    private var isOpenLoggerPreference: SwitchPreference? = null
    private var isNomediaPreference: SwitchPreference? = null
    private var isUseSystemFontPreference: SwitchPreference? = null

    private var mContext: Context? = null

    private val onCheckedListener = Preference.OnPreferenceChangeListener { preference, p2 ->
        when (preference.key) {

        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 改变xml配置文件指向
        preferenceManager.sharedPreferencesName = PreferenceHolder.FILE_NAME
        // 加载xml资源文件
        addPreferencesFromResource(R.xml.preference_settings)
        // 绑定View
        bindView()
        // 获得上下文
        mContext = activity

        init()
    }

    private fun bindView() {
        onClearImageCachePreference = this.findPreference(KEY_CLEAR_IMAGECACHE)
        onDownloadPathPreference = this.findPreference(KEY_DOWNLOADPATH)
        onSafeModePreference = this.findPreference(KEY_SAFE_MODE) as SwitchPreference
        isNomediaPreference = this.findPreference(KEY_IS_NOMEDIA) as SwitchPreference
        isOpenLoggerPreference = this.findPreference(KEY_IS_OPEN_LOGGER) as SwitchPreference
        isUseSystemFontPreference = this.findPreference(KEY_USE_SYSYTEM_FONT) as SwitchPreference
    }

    private fun init() {
        onDownloadPathPreference!!.summary = Config.DIR_IMAGE_DOWNLOAD.absolutePath
    }

    override fun onPreferenceTreeClick(
        preferenceScreen: PreferenceScreen,
        preference: Preference
    ): Boolean {
        when (preference.key) {
            KEY_USE_SYSYTEM_FONT -> {
                PreferenceHolder.putBoolean(
                    KEY_USE_SYSYTEM_FONT,
                    isUseSystemFontPreference!!.isChecked
                )
                if (isUseSystemFontPreference!!.isChecked)
                    App.setFont("")
                else
                    App.setFont(App.FONT_LOCAL)
            }
            KEY_CLEAR_IMAGECACHE -> try {
                val cacheSize = GlideCacheUtil.instance.getCacheSize(App.context)
                GlideCacheUtil.instance.clearImageAllCache(App.context)
                Toasty.info(activity, "清除缓存 $cacheSize").show()
            } catch (e: Exception) {
                Toasty.error(activity, "缓存清除失败").show()
            }

            KEY_SAFE_MODE -> {
                PreferenceHolder.putBoolean(KEY_SAFE_MODE, onSafeModePreference!!.isChecked)
                SiteManager.reloadSites(Config.DIR_SITE_PACK)
            }
            KEY_IS_OPEN_LOGGER -> {
            }
            KEY_IS_NOMEDIA -> {
                val file = File(Config.DIR_IMAGE_DOWNLOAD, ".nomedia")
                if (isNomediaPreference!!.isChecked) {
                    if (!file.exists()) {
                        try {
                            file.createNewFile()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                } else {
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference)
    }

    companion object {
        val KEY_CLEAR_IMAGECACHE = "onClearImageCache"
        //public static final String KEY_IMPORT_COLLECTIONS = "onImportCollections";
        val KEY_IS_OPEN_LOGGER = "isOpenLogger"
        val KEY_DOWNLOADPATH = "onDownloadPath"
        val KEY_IS_NOMEDIA = "isNomedia"
        val KEY_USE_SYSYTEM_FONT = "useSystemFont"
        val KEY_SAFE_MODE = "onSafeMode"
    }
}
