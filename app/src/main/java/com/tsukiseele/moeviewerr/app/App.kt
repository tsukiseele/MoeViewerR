package com.tsukiseele.moeviewerr.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Bundle
import com.bumptech.glide.Glide
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.debug.CrashHandler
import com.tsukiseele.moeviewerr.dataholder.PreferenceHolder
import com.tsukiseele.moeviewerr.ui.fragments.SettingsFragment
import com.tsukiseele.moeviewerr.utils.TextUtil
import com.tsukiseele.sakurawler.SiteManager
import es.dmoral.toasty.Toasty
import java.util.*

class App : Application() {
    // 活动列表
    private var activityList: MutableList<Activity>? = null

    override fun onCreate() {
        super.onCreate()
        init()

        initAppInfo()

        initAppResource()

        Config.initAppDirectory()

        SiteManager.reloadSites(Config.DIR_SITE_RULE)

        Glide.get(this)
    }

    private fun initAppResource() {
        // 加载字体
        if (!PreferenceHolder.getBoolean(SettingsFragment.KEY_USE_SYSYTEM_FONT, false))
            setFont(FONT_LOCAL)

        Toasty.Config.getInstance()
            .setToastTypeface(Typeface.DEFAULT)
            .allowQueue(false)
            .apply()
        // 加载下拉进度条颜色
        swipeRefreshColors = intArrayOf(
            R.color.primary,
            R.color.color_teal_500,
            R.color.color_deep_purple_500,
            R.color.color_blue_500
        )
    }

    private fun init() {
        app = this
        // 注册异常处理器
        CrashHandler.init(this)
        // 初始化活动管理器
        activityList = Collections.synchronizedList(ArrayList())
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                activityList!!.add(activity)
            }

            override fun onActivityStarted(p1: Activity) {}

            override fun onActivityResumed(p1: Activity) {}

            override fun onActivityPaused(p1: Activity) {}

            override fun onActivityStopped(p1: Activity) {}

            override fun onActivitySaveInstanceState(p1: Activity, p2: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                activityList!!.remove(activity)
            }
        })
    }

    private fun initAppInfo() {
        // 获取软件信息
        packageInfo = getPackageInfo(this)
    }

    // 退出应用
    fun exit() {
        if (activityList != null) {
            for (i in activityList!!.indices) {
                val activty = activityList!![i]
                activty.finish()
                activityList!!.remove(activty)
            }

            activityList!!.clear()
        }
    }

    companion object {
        // 默认字体
        val FONT_LOCAL = "fonts/InfoDisplayOT-Medium.otf"
        var app: App? = null
            private set

        // 程序信息
        var packageInfo: PackageInfo? = null
        // 颜色列表
        var swipeRefreshColors: IntArray? = null
            private set

        val context: Context
            get() = app!!.applicationContext

        fun getPackageInfo(context: Context): PackageInfo? {
            var packageInfo: PackageInfo? = null
            try {
                packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_CONFIGURATIONS
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return packageInfo
        }

        // 变更应用字体
        fun setFont(assetFontPath: String) {
            if (assetFontPath.isEmpty())
                changeSystemFont(Typeface.DEFAULT)
            else
                changeSystemFont(Typeface.createFromAsset(app!!.assets, assetFontPath))
        }

        fun changeSystemFont(tf: Typeface) {
            try {
                (fun (fieldName: String) {
                    val f = Typeface::class.java.getDeclaredField(fieldName)
                    f.isAccessible = true
                    f.set(null, tf)
                }).also {
                    it("DEFAULT")
                    it("DEFAULT_BOLD")
                    it("SANS_SERIF")
                    it("SERIF")
                    it("MONOSPACE")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /*
	 * 重新启动应用
	 *
	 */
        fun restartApplication() {
            app!!.exit()
            val intent = app!!.packageManager.getLaunchIntentForPackage(app!!.packageName)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            app!!.startActivity(intent)
        }

        /*
     * 判断是否有网络连接
     * @param context
     * @return
     *
	 */
        fun isNetworkConnected(context: Context?): Boolean {
            if (context != null) {
                val connectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = connectivityManager.activeNetworkInfo
                if (networkInfo != null)
                    return networkInfo.isAvailable
            }
            return false
        }
    }
}
