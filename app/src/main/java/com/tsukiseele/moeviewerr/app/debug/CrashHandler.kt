package com.tsukiseele.moeviewerr.app.debug

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.Looper
import android.util.Log
import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.app.App
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.dataholder.PreferenceHolder
import com.tsukiseele.moeviewerr.ui.fragments.SettingsFragment
import com.tsukiseele.moeviewerr.utils.AndroidUtil
import com.tsukiseele.sakurawler.utils.IOUtil
import es.dmoral.toasty.Toasty
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Thread.UncaughtExceptionHandler
import java.text.SimpleDateFormat
import java.util.*

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 * 必须使用Appliction进行初始化，否则可能导致内存泄漏。
 */
@SuppressLint("StaticFieldLeak")
object CrashHandler : UncaughtExceptionHandler {
    private val TAG = "CrashHandler"
    // 系统默认的UncaughtException处理类
    private var mDefaultHandler: UncaughtExceptionHandler? = null
    // Application的Context对象
    private var context: Context? = null
    // Application
    private var application: Application? = null
    // 用来存储设备信息和异常信息
    private val infos = HashMap<String, String>()
    // 用于格式化日期,作为日志文件名的一部分
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    /**
     * 初始化
     *
     * @param context
     */
    fun init(app: Application) {
        context = app.applicationContext

        application = app
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 当UncaughtException发生时会调用该方法
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
//        handleException(ex)
//        mDefaultHandler?.uncaughtException(thread, ex)

        if (!handleException(ex)) {
            // 若未处理异常则让系统默认的异常处理器来处理
            mDefaultHandler?.uncaughtException(thread, ex)
        } else {
            try {
                (application as App).exit()
            } catch (e: Throwable) {
            }
            Thread.sleep(1500)
            // 终止进程
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null)
            return false

        if (ex is OutOfMemoryError)
            return true

//        if (checkRestart(ex))
//            return true

        try {
            //收集设备参数信息
            findVersionInfo(context!!)
            //保存日志文件
            saveCrashInfoToFile(ex)
        } catch (e: Throwable) { }

        Thread(Runnable {
            if (PreferenceHolder.getBoolean(SettingsFragment.KEY_IS_OPEN_LOGGER, true)) {
                val writer = StringWriter()
                ex.printStackTrace(PrintWriter(writer))
                val result = writer.toString()
                writer.close()

                Looper.prepare()
                AndroidUtil.putTextIntoClip(context!!.resources.getString(R.string.app_name) + "_" + App.packageInfo!!.versionName + " :\n" + result)
                Toasty.error(context!!, "崩溃了！异常报告已导出到剪切板\n多次出现该异常可以向开发者报告此问题").show()
                Looper.loop()
            } else {
                Looper.prepare()
                Toasty.error(context!!, "出现致命异常！应用即将终止").show()
                Looper.loop()
            }
        }).start()

        return true
    }

    /**
     * 检查是否需要重启
     *
     */
    private fun checkRestart(ex: Throwable): Boolean {
        val writer = StringWriter()
        val pw = PrintWriter(writer)
        ex.printStackTrace(pw)
        val message = writer.toString()
        pw.close()
        if (message.contains("make sure class name exists, is public, and has an empty constructor that is public") && message.contains(
                "Fragment\$InstantiationException: Unable to instantiate fragment"
            )
        ) {
            App.restartApplication()
            return true
        }
        return false
    }

    /**
     * 收集版本信息
     * @param ctx
     */
    fun findVersionInfo(ctx: Context) {
        try {
            val pm = ctx.packageManager
            val pi = pm.getPackageInfo(ctx.packageName, PackageManager.GET_ACTIVITIES)
            if (pi != null) {
                val versionName = if (pi.versionName == null) "null" else pi.versionName
                val versionCode = pi.versionCode.toString() + ""
                infos["versionName"] = versionName
                infos["versionCode"] = versionCode
            }
        } catch (e: NameNotFoundException) {
            Log.e(TAG, "an error occured when collect package info", e)
        }

        findFieldInfo(Build::class.java)
        findFieldInfo(Build.VERSION::class.java)
    }

    /**
     *
     *
     *
     */
    private fun findFieldInfo(type: Class<*>) {
        val fields = type.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                infos[field.name] = field.get(null)!!.toString()
            } catch (e: Exception) {
                Log.e(TAG, "an error occured when collect crash info", e)
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return  返回文件名称,便于将文件传送到服务器
     */
    private fun saveCrashInfoToFile(ex: Throwable): String? {

        val sb = StringBuffer()
        for ((key, value) in infos) {
            sb.append("$key = $value\n")
        }

        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause: Throwable? = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result = writer.toString()
        sb.append(result)

        try {
            if (PreferenceHolder.getBoolean(SettingsFragment.KEY_IS_OPEN_LOGGER, true)) {
                val timestamp = System.currentTimeMillis()
                val time = formatter.format(Date())
                val fileName = "/Crash-$time-$timestamp.log"
                val dir = Config.DIR_CRASH_LOGGER
                if (!dir.exists()) dir.mkdirs()
                if (dir.listFiles()!!.size > 100)
                    IOUtil.deleteDirectoryAllFile(dir.absolutePath)
                File(dir, fileName).writeText(sb.toString())
                return fileName
            }
        } catch (e: Exception) {
            Log.e(TAG, "an error occured while writing file...", e)
        }

        return null
    }

//    companion object {


    // 静态CrashHandler实例
    // 获取CrashHandler实例 ,单例模式
//        val get = CrashHandler()
//    }
}  
