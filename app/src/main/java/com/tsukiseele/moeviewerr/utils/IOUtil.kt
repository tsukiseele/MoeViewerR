package com.tsukiseele.moeviewerr.utils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.Serializable
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object IOUtil {
    val DEFAULT_CHARSET = "UTF-8"
    val BUFF_SIZE = 4096

    val Byte = 1L
    val KB = 1024L
    val MB = 1048576L
    val GB = 1073741824L
    val TB = 1099511627776L

    interface OnFileScanCallback {
        fun onScan(file: File): Boolean
    }

    enum class FileSize(size: Long) {
        Byte(IOUtil.Byte),
        KB(IOUtil.KB),
        MB(IOUtil.MB),
        GB(IOUtil.GB),
        TB(IOUtil.TB);

        private val size: Long

        init {
            this.size = size
        }

        fun size(): Long {
            return size
        }

        override fun toString(): String {
            return this.name
        }
    }

    fun formatDataSize(size: Double): String {
        val formater = DecimalFormat("####.00")
        if (size < 0)
            return size.toString()
        return if (size < KB)
            size.toString() + FileSize.Byte.name
        else if (size < MB)
            formater.format(size / KB) + FileSize.KB.name
        else if (size < GB)
            formater.format(size / MB) + FileSize.MB.name
        else if (size < TB)
            formater.format(size / GB) + FileSize.GB.name
        else
            formater.format(size / TB) + FileSize.TB.name
    }

    fun printText(
        filePath: String,
        text: String,
        charset: String,
        autoFlush: Boolean,
        append: Boolean
    ): Boolean {
        var pw: PrintWriter? = null
        var isFinish = true
        try {
            pw = PrintWriter(
                OutputStreamWriter(
                    FileOutputStream(filePath, append), charset
                ), autoFlush
            )
            pw.print(text)
        } catch (e: Exception) {
            isFinish = false
        } finally {
            close(pw)
        }
        return isFinish
    }

    @JvmOverloads
    fun printText(
        filePath: String,
        text: String,
        charset: String,
        append: Boolean = false
    ): Boolean {
        return printText(filePath, text, charset, false, append)
    }

    fun printText(filePath: String, text: String, autoFlush: Boolean): Boolean {
        return printText(filePath, text, DEFAULT_CHARSET, true, false)
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun writeText(
        filePath: String,
        text: String,
        charset: String = DEFAULT_CHARSET,
        isAppend: Boolean = false
    ) {
        var writer: BufferedWriter? = null
        try {
            writer = BufferedWriter(
                OutputStreamWriter(
                    FileOutputStream(filePath, isAppend), charset
                )
            )
            writer.write(text)
        } finally {
            close(writer)
        }
    }

    @Throws(IOException::class)
    fun writeText(filePath: String, text: String, isAppend: Boolean) {
        writeText(filePath, text, DEFAULT_CHARSET, isAppend)
    }

    @Throws(IOException::class)
    fun readText(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        try {
            var line: String
            while (reader.readLine().also { line = it } != null)
                stringBuilder.append(line)
        } finally {
            close(reader)
        }
        return stringBuilder.toString()
    }

//    @Throws(IOException::class)
//    @JvmOverloads
//    fun readText(filePath: String, charset: String = DEFAULT_CHARSET): String {
//        File(filePath).readText(Charset.forName(charset))
//        var reader: InputStreamReader? = null
//        val text: String
//        try {
//            reader = FileInputStream(filePath).buffered().reader(Charset.forName(charset))
//            text = reader.readText()
//            reader = BufferedReader(InputStreamReader(FileInputStream(filePath), charset))
//            reader.buffered().read
//            var line: String
//            while (reader.readLine().also { line = it } != null)
//        } finally {
//            close(reader)
//        }
//        return text
//    }

    @Throws(IOException::class)
    fun writeBytes(filePath: String, inputStream: InputStream) {
        var bos: BufferedOutputStream? = null
        var bis: BufferedInputStream? = null
        try {
            bis = BufferedInputStream(inputStream)
            bos = BufferedOutputStream(
                FileOutputStream(filePath)
            )
            val buff = ByteArray(BUFF_SIZE)
            var length: Int
            while (bis.read(buff).also { length = it } != -1)
                bos.write(buff, 0, length)
        } finally {
            close(bos)
            close(bis)
        }
    }

    @Throws(IOException::class)
    fun writeBytes(filePath: String, datas: ByteArray) {
        var bos: BufferedOutputStream? = null
        try {
            bos = BufferedOutputStream(
                FileOutputStream(filePath)
            )
            bos.write(datas)
        } finally {
            close(bos)
        }
    }

    @Throws(IOException::class)
    fun readBytes(inputStream: InputStream): ByteArray {
        var bis: BufferedInputStream? = null
        var baos: ByteArrayOutputStream? = null
        try {
            bis = BufferedInputStream(inputStream)
            baos = ByteArrayOutputStream()

            val buff = ByteArray(BUFF_SIZE)
            var length: Int
            while (bis.read(buff).also { length = it } != -1)
                baos.write(buff, 0, length)
        } finally {
            close(bis)
            close(baos)
        }
        return baos!!.toByteArray()
    }

    @Throws(IOException::class)
    fun readBytes(filePath: String): ByteArray {
        var bis: BufferedInputStream? = null
        var baos: ByteArrayOutputStream? = null
        val buff = ByteArray(BUFF_SIZE)
        try {
            bis = BufferedInputStream(FileInputStream(filePath))
            baos = ByteArrayOutputStream()

            var length: Int
            while (bis.read(buff).also { length = it } != -1)
                baos.write(buff, 0, length)
        } finally {
            close(bis)
            close(baos)
        }
        return baos!!.toByteArray()
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun readSerializable(filePath: String): Any {
        var ois: ObjectInputStream? = null
        val obj: Any
        try {
            ois = ObjectInputStream(FileInputStream(filePath))
            obj = ois.readObject() as Any
        } finally {
            close(ois)
        }
        return obj
    }

    @Throws(IOException::class)
    fun <T : Serializable> writeSerializable(filePath: String, `object`: T) {
        var oos: ObjectOutputStream? = null
        try {
            oos = ObjectOutputStream(FileOutputStream(filePath))
            oos.writeObject(`object`)
        } finally {
            close(oos)
        }
    }

    fun mkdirs(file: File?): Boolean {
        if (file == null)
            return false
        return if (!file.exists()) file.mkdirs() else false
    }

    fun mkdirsParent(file: File?): Boolean {
        if (file == null)
            return false
        return if (file.parentFile != null && !file.parentFile!!.exists()) file.parentFile!!.mkdirs() else false
    }

    fun mkdirs(filePath: String): Boolean {
        return if (TextUtil.isEmpty(filePath)) false else mkdirs(File(filePath))
    }

    fun mkdirsParent(filePath: String): Boolean {
        return if (TextUtil.isEmpty(filePath)) false else mkdirsParent(File(filePath))
    }

    /**
     * 获取指定文件夹内所有文件大小的和
     *
     * @param file file
     * @return size
     * @throws Exception
     */
    fun getDirectoryAllFileSize(file: File): Long {
        var size: Long = 0
        val fileList = file.listFiles() ?: return 0
        for (fileItem in fileList) {
            if (fileItem.isFile) {
                size += fileItem.length()
            } else {
                size += getDirectoryAllFileSize(fileItem)
            }
        }
        return size
    }

    fun getDirectoryAllFileSize(dirPath: String): Long {
        return getDirectoryAllFileSize(File(dirPath))
    }

    fun deleteFile(path: String): Boolean {
        return if (TextUtil.isEmpty(path)) false else deleteFile(File(path))
    }

    fun deleteFile(file: File?): Boolean {
        return file?.delete() ?: false
    }

    /**
     * 递归删除指定目录下的文件
     *
     * @param dir filePath
     */
    fun deleteDirectoryAllFile(dir: File?) {
        if (dir == null)
            return
        if (dir.exists()) {
            if (dir.isDirectory) {
                val files = dir.listFiles()
                for (file in files!!)
                    if (file.isFile)
                        file.delete()
                    else
                        deleteDirectoryAllFile(file)
                dir.delete()
            } else {
                dir.delete()
            }
        }
    }

    fun deleteDirectoryAllFile(dirPath: String) {
        deleteDirectoryAllFile(File(dirPath))
    }

    @Throws(IOException::class)
    fun copyFile(fromPath: String, toPath: String): Boolean {
        if (TextUtil.isEmpty(fromPath) || TextUtil.isEmpty(toPath))
            return false
        mkdirsParent(toPath)
        writeBytes(toPath, readBytes(fromPath))
        return true
    }

    @Throws(IOException::class)
    fun copyFile(fromPath: File, toPath: File): Boolean {
        return copyFile(fromPath.absolutePath, toPath.absolutePath)
    }

    fun copyDirectory(fromPath: String, toPath: String): Int {
        var counter = 0
        val files = scanDirectory(File(fromPath))
        for (file in files) {
            try {
                copyFile(
                    file.absolutePath,
                    file.absolutePath.replace(
                        File(fromPath).parentFile!!.absolutePath.toRegex(),
                        toPath
                    )
                )
            } catch (e: IOException) {
                counter++
                continue
            }

        }
        return counter
    }

    @Throws(IOException::class)
    fun moveFile(fromPath: String, toPath: String): Boolean {
        if (TextUtil.isEmpty(fromPath) || TextUtil.isEmpty(toPath))
            return false
        mkdirsParent(toPath)
        writeBytes(fromPath, readBytes(toPath))
        deleteFile(fromPath)
        return true
    }

    fun moveDirectory(fromPath: String, toPath: String): Boolean {
        if (TextUtil.isEmpty(fromPath) || TextUtil.isEmpty(toPath))
            return false
        copyDirectory(fromPath, toPath)
        deleteDirectoryAllFile(fromPath)
        return true
    }

    fun exists(path: String): Boolean {
        return if (!TextUtil.isEmpty(path)) exists(File(path)) else false
    }

    fun exists(file: File?): Boolean {
        return if (file != null && file.exists()) true else false
    }

    fun close(closeable: Closeable?): Boolean {
        try {
            if (closeable != null) {
                closeable.close()
                return true
            }
        } catch (e: IOException) {
            // 这里不作处理
        }

        return false
    }

    // 扫描目录
    fun scanDirectory(rootPath: String, vararg suffixs: String): List<File> {
        return scanDirectory(File(rootPath), *suffixs)
    }

    fun scanDirectory(rootPath: File, vararg suffixs: String): List<File> {
        return scanDirectory(rootPath, null, false, *suffixs)
    }

    fun scanDirectory(
        rootPath: File,
        containDirectory: Boolean,
        vararg suffixs: String
    ): List<File> {
        return scanDirectory(rootPath, null, containDirectory, *suffixs)
    }

    fun scanDirectory(
        rootPath: File?,
        callback: OnFileScanCallback?,
        containDirectory: Boolean,
        vararg suffixs: String
    ): List<File> {
        if (rootPath == null)
            throw NullPointerException("目录不能为null")
        if (!rootPath.exists())
            return ArrayList(0)
        var files: Array<File>? = null
        if (suffixs == null || suffixs.size == 0)
            files = rootPath.listFiles()
        else
            files = rootPath.listFiles(FileFilter { file ->
                if (file.isDirectory)
                    return@FileFilter true
                else
                    for (suffix in suffixs)
                    // 转换为小写后比较
                        if (file.name.toLowerCase().endsWith(suffix.toLowerCase()))
                            return@FileFilter true
                false
            })
        if (files == null)
            return ArrayList(0)
        val fileList = ArrayList<File>(files.size)
        for (file in files) {
            if (file.isDirectory) {
                val fs = scanDirectory(file, callback, containDirectory, *suffixs)
                if (fs != null)
                    fileList.addAll(fs)
                if (containDirectory)
                    fileList.add(file)
            } else {
                if (callback != null) {
                    if (callback.onScan(file))
                        fileList.add(file)
                } else {
                    fileList.add(file)
                }
            }
        }
        return fileList
    }

    fun getRandomFile(directory: File?, suffixs: Array<String>): File? {
        if (directory == null || !directory.isDirectory)
            return null
        val images = directory.listFiles(FilenameFilter { parent, name ->
            for (suffix in suffixs)
                if (name.endsWith(suffix))
                    return@FilenameFilter true
            false
        })
        return if (images != null && images.size > 0)
            images[(Math.random() * images.size).toInt()]
        else
            null
    }

    fun getUrlFileName(url: String): String {
        val nameStart = url.lastIndexOf('/')
        val nameEnd = url.lastIndexOf('.')
        if (nameStart != -1) {
            if (nameStart > nameEnd)
                return url.substring(nameStart + 1)
            val name = url.substring(nameStart + 1, nameEnd)
            val suffix = """[0-9a-zA-Z]+""".toRegex()
                .find(url.substring(nameEnd))?.value
            return if (suffix == null) url.substring(nameStart + 1)
                else "$name.$suffix"
        } else {
            return url
        }
    }

    fun appendFilename(file: File, append: String): File {
        val end = file.name.indexOf(".")
        val oldName = file.name
        val newName: String
        if (end != -1) {
            newName = oldName.substring(0, end) + append + oldName.substring(end)
        } else {
            newName = file.name + append
        }
        return File(file.parent, newName)
    }

    fun getFileSuffix(filename: String): String {
        if (TextUtil.isEmpty(filename)) {
            return ""
        } else {
            var index = filename.lastIndexOf('.')
            return if (index != -1 && ++index < filename.length) filename.substring(
                index,
                filename.length
            ) else ""
        }
    }

    fun getFileSuffix(file: File): String {
        return getFileSuffix(file.absolutePath)
    }

    fun getWindowsFilename(filename: String): String {
        return filename.replace(":".toRegex(), "%3A")
            .replace("\\*".toRegex(), "%2A")
            .replace("\\?".toRegex(), "%3F")
            .replace("\"".toRegex(), "%22")
            .replace("<".toRegex(), "%3C")
            .replace(">".toRegex(), "%3E")
            .replace("\\|".toRegex(), "%7C")
    }

    fun getWindowsPath(filename: String): String {
        return filename.replace("\\*".toRegex(), "%2A")
            .replace("\\?".toRegex(), "%3F")
            .replace("\"".toRegex(), "%22")
            .replace("<".toRegex(), "%3C")
            .replace(">".toRegex(), "%3E")
            .replace("\\|".toRegex(), "%7C")
    }

    fun getPureFilename(filename: String): String {
        return filename.replace("\\.\\.\\\\|\\\\|/|../".toRegex(), "-")
    }

    object ZipUtil {
        interface OnCompressProgress {
            fun onProgress(file: File, length: Long): Boolean
        }

        @Throws(IOException::class)
        @JvmOverloads
        fun createZip(directory: File, onCompressProgress: OnCompressProgress? = null): File {
            var zos: ZipOutputStream? = null
            var bis: BufferedInputStream? = null
            val zip = File("$directory.zip")
            try {
                zos = ZipOutputStream(FileOutputStream(zip))
                val fileList = scanDirectory(directory)

                for (f in fileList) {
                    bis = BufferedInputStream(FileInputStream(f))
                    val entry = ZipEntry(f.absolutePath.replace(directory.absolutePath, ""))
                    zos.putNextEntry(entry)
                    val buff = ByteArray(BUFF_SIZE)
                    var length: Long = 0
                    var pos = 0
                    do {
                        pos = bis.read(buff)
                        if (pos <= 0)
                            break
                        zos.write(buff, 0, pos)
                        length += pos.toLong()
                        onCompressProgress?.onProgress(f, length)
                    } while (true)
//                    while ((pos = bis.read(buff)) > 0) {
//                        zos.write(buff, 0, pos)
//                        length += pos.toLong()
//                        onCompressProgress?.onProgress(f, length)
//                    }
                    bis.close()
                    zos.closeEntry()
                }
            } finally {
                bis?.close()
                zos?.close()
            }
            return zip
        }
    }
}
