package com.tsukiseele.moeviewerr.dataholder

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.tsukiseele.moeviewerr.ui.service.DownloadService
import com.tsukiseele.moeviewerr.app.App

class DownloadHolder private constructor() : ServiceConnection {
    var binder: DownloadService.DownloadBinder? = null
        private set

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        if (binder is DownloadService.DownloadBinder) {
            this.binder = binder
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {

    }

    companion object {
        private var downloadHolder: DownloadHolder? = null

        val instance: DownloadHolder?
            get() {
                if (downloadHolder == null)
                    bind(App.context)
                return downloadHolder

            }

        fun bind(context: Context): DownloadHolder {
            if (downloadHolder == null) {
                synchronized(DownloadHolder::class.java) {
                    if (downloadHolder == null)
                        downloadHolder = DownloadHolder()
                }
            }
            context.bindService(
                Intent(context, DownloadService::class.java),
                downloadHolder!!,
                Service.BIND_AUTO_CREATE
            )
            return downloadHolder!!
        }

        fun unbind(context: Context) {
            try {
                context.unbindService(downloadHolder!!)
            } catch (e: Exception) {
                // 如果服务未绑定，再调用解绑方法会抛出异常
            }

        }
    }
}
