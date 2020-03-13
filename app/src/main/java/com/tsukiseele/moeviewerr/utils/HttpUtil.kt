package com.tsukiseele.moeviewerr.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.Socket
import java.net.URL
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

object HttpUtil {
    val DEFAULT_UA =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11"

    @Throws(IOException::class)
    @JvmOverloads
    fun requestHtmlDocument(url: String, timeout: Int = 10000): String? {
        var connect: HttpURLConnection? = null
        var bufferedReader: BufferedReader? = null
        val sb = StringBuilder()
        connect = getHttpURLConnection(url, timeout)
        connect.connect()
        if (HttpURLConnection.HTTP_OK == connect.responseCode) {
            bufferedReader = BufferedReader(InputStreamReader(connect.inputStream))
            var line: String? = null
            while ({ line = bufferedReader.readLine(); line }() != null)
                sb.append(line).append('\n')
        } else {
            return null
        }

        connect?.disconnect()
        try {
            bufferedReader?.close()
        } catch (ex: IOException) {

        }

        return sb.toString()
    }

    // 网络连接配置
    @Throws(java.net.ProtocolException::class, MalformedURLException::class, IOException::class)
    fun getHttpURLConnection(url: String, timeout: Int): HttpURLConnection {
        var conn: HttpURLConnection? = null

        //1.定义一个URL对象
        val mUrl = URL(url)
        //2.通过url获取一个HttpURLConnection对象
        conn = mUrl.openConnection() as HttpURLConnection
        // 伪装成浏览器，针对部分无法访问的网站
        conn.setRequestProperty("User-Agent", DEFAULT_UA)
        // 3.设置请求方式
        conn.requestMethod = "GET"
        // 4.设置超时时间
        conn.connectTimeout = timeout
        conn.readTimeout = timeout

        return conn
    }

    @Throws(java.net.ProtocolException::class, MalformedURLException::class, IOException::class)
    fun getHttpURLConnection(
        url: String,
        headers: Map<String, String>,
        timeout: Int
    ): HttpURLConnection {
        var conn: HttpURLConnection? = null

        // 1.定义一个URL对象
        val mUrl = URL(url)
        // 2.通过url获取一个HttpURLConnection对象
        conn = mUrl.openConnection() as HttpURLConnection
        // 3.添加请求头，针对部分无法访问的网站
        for ((key, value) in headers)
            conn.addRequestProperty(key, value)
        // 3.设置请求方式
        conn.requestMethod = "GET"
        // 4.设置超时时间
        conn.connectTimeout = timeout
        conn.readTimeout = timeout

        return conn
    }


    class TLSSocketFactory @Throws(KeyManagementException::class, NoSuchAlgorithmException::class)
    constructor() : SSLSocketFactory() {
        private val delegate: SSLSocketFactory

        init {
            val context = SSLContext.getInstance("TLS")
            context.init(null, null, null)
            delegate = context.socketFactory
        }

        override fun getDefaultCipherSuites(): Array<String> {
            return delegate.defaultCipherSuites
        }

        override fun getSupportedCipherSuites(): Array<String> {
            return delegate.supportedCipherSuites
        }

        @Throws(IOException::class)
        override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket? {
            return enableTLSOnSocket(delegate.createSocket(s, host, port, autoClose))
        }

        @Throws(IOException::class)
        override fun createSocket(host: String, port: Int): Socket? {
            return enableTLSOnSocket(delegate.createSocket(host, port))
        }

        @Throws(IOException::class)
        override fun createSocket(
            host: String,
            port: Int,
            localHost: InetAddress,
            localPort: Int
        ): Socket? {
            return enableTLSOnSocket(
                delegate.createSocket(
                    host, port, localHost,
                    localPort
                )
            )
        }

        @Throws(IOException::class)
        override fun createSocket(host: InetAddress, port: Int): Socket? {
            return enableTLSOnSocket(delegate.createSocket(host, port))
        }

        @Throws(IOException::class)
        override fun createSocket(
            address: InetAddress,
            port: Int,
            localAddress: InetAddress,
            localPort: Int
        ): Socket? {
            return enableTLSOnSocket(delegate.createSocket(address, port, localAddress, localPort))
        }

        private fun enableTLSOnSocket(socket: Socket?): Socket? {
            if (socket != null && socket is SSLSocket) {

                val protocols = socket.enabledProtocols
                val supports = ArrayList<String>()
                if (protocols != null && protocols.size > 0) {
                    supports.addAll(Arrays.asList(*protocols))
                }
                Collections.addAll(supports, "TLSv1.1", "TLSv1.2")
                socket.enabledProtocols = supports.toTypedArray()
            }
            return socket
        }
    }

    fun getNetworkFileLength(url: String, headers: Map<String, String>, timeout: Int): Long {
        var conn: HttpURLConnection? = null
        try {
            conn = getHttpURLConnection(url, headers, timeout)
            conn.connect()
            return if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.contentLength.toLong()
            } else {
                -1
            }
        } catch (e: Exception) {

        } finally {
            conn?.disconnect()
        }
        return -1
    }

    fun getUrlHostName(url: String) : String {
        val URL = URL(url)
        return "${URL.protocol}://${URL.host}"
    }
}
