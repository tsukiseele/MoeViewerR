package com.tsukiseele.moeviewerr.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import android.view.View
import com.tsukiseele.moeviewerr.R

// 启动新的Activity并淡入
fun Activity.startActivityOfFadeAnimation(intent: Intent) {
    val compat = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
    ActivityCompat.startActivity(this, intent, compat.toBundle())
}

fun Activity.startActivityOfFadeAnimation(clazz: Class<*>) {
    val compat = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
    ActivityCompat.startActivity(this, Intent(this, clazz), compat.toBundle())
}

object ActivityUtil {
    // 启动新的Activity并从一个范围过渡到全屏
    fun startActivityOfScaleUpAnimation(context: Context, view: View, intent: Intent) {
        val compat = ActivityOptionsCompat.makeScaleUpAnimation(
            view,
            view.width / 2, view.height / 2, //拉伸开始的坐标
            0, 0 //拉伸开始的区域大小，这里用（0, 0）表示从无到全屏
        )
        ActivityCompat.startActivity(context, intent, compat.toBundle())
    }

    // 启动新的Activity并从一个视图过渡到另一个视图
    fun startActivityOfSceneTransition(activity: Activity, view: View, intent: Intent) {
        val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(
            activity, view,
            activity.resources.getString(R.string.scene_transition)
        )
        ActivityCompat.startActivity(activity, intent, compat.toBundle())
    }
}
