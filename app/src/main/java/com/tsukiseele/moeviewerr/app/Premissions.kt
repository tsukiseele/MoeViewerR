package com.tsukiseele.moeviewerr.app

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object Premissions {
    val REQUEST_EXTERNAL_STORAGE = 1

    private val PERMISSIONS_STORAGE = arrayOf(
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )

    /**
     *
     * @param activity
     * @return 是否已拥有权限
     */
    fun verifyStoragePermissions(activity: Activity): Boolean {
        try {
            //检测是否有写的权限
            val permission = ActivityCompat.checkSelfPermission(
                activity,
                "android.permission.WRITE_EXTERNAL_STORAGE"
            )

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
                )
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }
}
