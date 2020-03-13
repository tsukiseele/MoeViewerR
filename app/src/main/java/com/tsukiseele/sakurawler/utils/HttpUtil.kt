package com.tsukiseele.sakurawler.utils

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.URL

object HttpUtil {
    @Throws(IOException::class)
    fun requestStream(
        url: String?,
        headers: Map<String, String?>
    ): InputStream {
        val connection = URL(url).openConnection() as HttpURLConnection
        val inputStream: InputStream
        connection.readTimeout = 10000
        connection.connectTimeout = 10000
        for (key in headers.keys) connection.addRequestProperty(key, headers[key])
        connection.connect()
        inputStream = if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream
        } else {
            throw SocketException("Connect failed! response code: " + connection.responseCode)
        }
        return inputStream
    }
}