package com.tsukiseele.moeviewerr

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.app.Premissions
import com.tsukiseele.moeviewerr.ui.activitys.abst.BaseActivity
import com.tsukiseele.moeviewerr.utils.DensityUtil
import com.tsukiseele.moeviewerr.utils.startActivityOfFadeAnimation
import com.tsukiseele.sakurawler.utils.IOUtil
import es.dmoral.toasty.Toasty
import java.util.*

// 
class LaunchActivity : BaseActivity() {
    private var launcherImage: ImageView? = null
    private var launcherMessage: TextView? = null
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot) {
            finish()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val decorView = window.decorView
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            decorView.systemUiVisibility = option
            window.statusBarColor = resources.getColor(android.R.color.transparent)
        }
        setContentView(R.layout.activity_launch)

        this.findViewById<ImageView>(R.id.activityLaunch_ImageView)?.let {
            // 加载启动图
            try {
                // 如果收藏夹中有图片，则使用，否则遍历
                val image = IOUtil.getRandomFile(
                    Config.DIR_IMAGE_SAVE,
                    arrayOf(".jpg", ".png", ".jpeg", ".webp")
                )
                if (image != null && image.length() != 0L) {
                    val screenSize = DensityUtil.getScreenSize(this)
                    Glide.with(this)
                        .load(image)
                        .override(screenSize[0], screenSize[1])
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .error(R.drawable.tsukiko)
                        .transition(DrawableTransitionOptions().transition(R.anim.anim_expand))
                        .into(it)
                } else {
                    it.setImageResource(R.drawable.tsukiko)
                }
            } catch (e: Exception) {
                it.setImageResource(R.drawable.tsukiko)
            }
        }

        // 权限申请
        if (Premissions.verifyStoragePermissions(this)) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    startMainActivity()
                    finish()
                }
            }, 800)
        }
    }

    /**
     * 权限申请返回结果
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults  申请结果数组
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Premissions.REQUEST_EXTERNAL_STORAGE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //同意权限申请
                startMainActivity()
                finish()
            } else { //拒绝权限申请
                Toasty.error(this, "缺少必要的权限，应用无法启动！").show()
                finish()
            }
            else -> {
            }
        }
    }

    override fun finish() {
        launcherImage = null
        if (bitmap != null && !bitmap!!.isRecycled) {
            bitmap!!.recycle()
            bitmap = null
        }
        super.finish()
    }

    fun startMainActivity() {
        this.startActivityOfFadeAnimation(MainActivity::class.java)
    }
}
