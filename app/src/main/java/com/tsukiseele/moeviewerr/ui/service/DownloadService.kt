package com.tsukiseele.moeviewerr.ui.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.tsukiseele.koradownload.KoraDownload
import com.tsukiseele.koradownload.base.DownloadTask

class DownloadService : Service() {
    private var downloadBinder: DownloadBinder? = null

    override fun onBind(intent: Intent): IBinder? {
        if (null == downloadBinder)
            downloadBinder = DownloadBinder()
        return downloadBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    inner class DownloadBinder : Binder() {
        val service: DownloadService
            get() = this@DownloadService

        fun execute(task: DownloadTask) {
            KoraDownload.execute(task)
        }

        fun pause(task: DownloadTask) {
            KoraDownload.pause(task)
        }

        fun resume(url: String) {
            KoraDownload.resume(url)
        }
    }
}
