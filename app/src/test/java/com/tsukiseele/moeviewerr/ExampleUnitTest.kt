package com.tsukiseele.moeviewerr

import android.graphics.Typeface
import com.tsukiseele.koradownload.SimpleTaskCallback
import com.tsukiseele.koradownload.base.DownloadTask
import com.tsukiseele.moeviewerr.app.Config
import com.tsukiseele.moeviewerr.utils.HttpUtil
import com.tsukiseele.moeviewerr.utils.IOUtil
import com.tsukiseele.moeviewerr.utils.OkHttpUtil
import com.tsukiseele.moeviewerr.utils.ToastUtil
import org.junit.Test
import java.lang.NullPointerException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val tagContent = "anus catgirl final_fantasy final_fantasy_xiv glasses iwbitu miqo'te uncensored "
        val tags = when {
            tagContent.contains(',') ->
                tagContent.split(",+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            tagContent.contains(';') ->
                tagContent.split(";+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            else ->
                tagContent.split("""[,;\s]+""".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }
        tags.forEach {
            println(it)
        }
    }
    @Test
    fun urlFilenameParseTest() {
        val url1 = "http://yuri.logacg.com/1911/c0ff853fa5aa24ee678fd3e7a6aab5ff.png"
        val url2 = "http://yuri.logacg.com/1911/c0ff853fa5aa24ee678fd3e7a6aab5ff.m3u8!single"
        val url3 = "http://yuri.logacg.com/1911/c0ff853fa5aa24ee678fd3e7a6aab5ff.webp!single?tags=yuri"
        val url4 = "http://yuri.logacg.com/1911/c0ff853fa5aa24ee678fd3e7a6aab5ffpng!single"
        val url5 = "http://yuri.logacg.com/1911/c0ff853fa5aa24ee678fd3e7a6aab5ff.!?="

        println(IOUtil.getUrlFileName(url1))
        println(IOUtil.getUrlFileName(url2))
        println(IOUtil.getUrlFileName(url3))
        println(IOUtil.getUrlFileName(url4))
        println(IOUtil.getUrlFileName(url5))
    }

    @Test
    fun downloadTest() {
        val url = "https://cs.sankakucomplex.com/data/5b/4a/5b4aff44a87b42344d67223f9b066c33.jpg?e=1583639503&m=ap-mrqzHnjeJtxOFtrGzAw"
//        DownloadHolder.get!!.binder!!.execute(
            DownloadTask.Builder(url)
                .toDir("D:\\CentBrowser Download")
//                .addHeader("Referer", HttpUtil.getUrlHostName(url))
//                .addParam("cover", "")
                .addCallback(object : SimpleTaskCallback() {
                    override fun onFailed(
                        task: DownloadTask?,
                        e: Throwable?
                    ) {}

                    override fun onSuccessful(task: DownloadTask?) {
//                        Util.notifySystemImageUpdate(
//                            context,
//                            task?.info()?.path
//                        )
                        println(task)
                        println("Successful")
                    }
                })
                .build().execute()
//        )
        Thread.sleep(20000)
    }
    @Test
    fun jsoupTest() {

    }
}
