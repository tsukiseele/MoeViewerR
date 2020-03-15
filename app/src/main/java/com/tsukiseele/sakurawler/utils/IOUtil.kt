package com.tsukiseele.sakurawler.utils

import java.io.*
import java.net.URLDecoder
import java.text.DecimalFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object IOUtil {
    const val DEFAULT_CHARSET = "UTF-8"
    const val BUFF_SIZE = 4096
    const val Byte = 1
    const val KB = 1024
    const val MB = 1048576
    const val GB = 1073741824
    const val TB = 1099511627776L

    fun formatDataSize(size: Double): String {
        val formater = DecimalFormat("####.00")
        if (size < 0) return size.toString()
        return if (size < KB) size.toString() + FileSize.Byte.name else if (size < MB) formater.format(
            size / KB
        ) + FileSize.KB.name else if (size < GB) formater.format(size / MB) + FileSize.MB.name else if (size < TB) formater.format(
            size / GB
        ) + FileSize.GB.name else formater.format(size / TB) + FileSize.TB.name
    }

    fun printText(
        filePath: String?,
        text: String?,
        charset: String?,
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
        filePath: String?,
        text: String?,
        charset: String?,
        append: Boolean = false
    ): Boolean {
        return printText(filePath, text, charset, false, append)
    }

    fun printText(filePath: String?, text: String?, autoFlush: Boolean): Boolean {
        return printText(
            filePath,
            text,
            DEFAULT_CHARSET,
            true,
            false
        )
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun writeText(
        filePath: String?,
        text: String?,
        charset: String? = DEFAULT_CHARSET,
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
    fun writeText(filePath: String?, text: String?, isAppend: Boolean) {
        writeText(filePath, text, DEFAULT_CHARSET, isAppend)
    }

    @Throws(IOException::class)
    fun readText(inputStream: InputStream?): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        try {
            var line: String?
            while (reader.readLine().also { line = it } != null) stringBuilder.append(line).append('\n')
        } finally {
            close(reader)
        }
        return stringBuilder.toString()
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun readText(
        filePath: String?,
        charset: String? = DEFAULT_CHARSET
    ): String {
        var reader: BufferedReader? = null
        val text = StringBuilder()
        try {
            reader = BufferedReader(InputStreamReader(FileInputStream(filePath), charset))
            var line: String?
            while (reader.readLine().also { line = it } != null) text.append(line).append('\n')
        } finally {
            close(reader)
        }
        return text.toString()
    }

    @Throws(IOException::class)
    fun writeBytes(filePath: String?, inputStream: InputStream) {
        var bos: BufferedOutputStream? = null
        var bis: BufferedInputStream? = null
        try {
            bis = BufferedInputStream(inputStream)
            bos = BufferedOutputStream(
                FileOutputStream(filePath)
            )
            var len: Int
            val buff = ByteArray(BUFF_SIZE)
            while (inputStream.read(buff).also { len = it } != -1) bos.write(buff, 0, len)
        } finally {
            close(bos)
            close(bis)
        }
    }

    @Throws(IOException::class)
    fun writeBytes(filePath: String?, datas: ByteArray?) {
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
    fun readByteArray(inputStream: InputStream?): ByteArray {
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
    fun readByteArray(filePath: String?): ByteArray {
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
    fun readSerializable(filePath: String?): Any {
        var ois: ObjectInputStream? = null
        val obj: Any
        try {
            ois = ObjectInputStream(FileInputStream(filePath))
            obj = ois.readObject()
        } finally {
            close(ois)
        }
        return obj
    }

    @Throws(IOException::class)
    fun <T : Serializable?> writeSerializable(filePath: String?, obj: T) {
        var oos: ObjectOutputStream? = null
        try {
            oos = ObjectOutputStream(FileOutputStream(filePath))
            oos.writeObject(obj)
        } finally {
            close(oos)
        }
    }

    fun mkdirs(file: File?): Boolean {
        if (file == null) return false
        return if (!file.exists()) file.mkdirs() else false
    }

    fun mkdirsParent(file: File?): Boolean {
        if (file == null) return false
        return if (file.parentFile != null && !file.parentFile.exists()) file.parentFile.mkdirs() else false
    }

    fun mkdirs(filePath: String?): Boolean {
        return if (filePath.isNullOrBlank()) false else mkdirs(File(filePath))
    }

    fun mkdirsParent(filePath: String?): Boolean {
        return if (filePath.isNullOrBlank()) false else mkdirsParent(File(filePath))
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
            size += if (fileItem.isFile) {
                fileItem.length()
            } else {
                getDirectoryAllFileSize(fileItem)
            }
        }
        return size
    }

    fun getDirectoryAllFileSize(dirPath: String?): Long {
        return getDirectoryAllFileSize(File(dirPath))
    }

    fun deleteFile(path: String?): Boolean {
        return if (path.isNullOrBlank()) false else deleteFile(File(path))
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
        if (dir == null) return
        if (dir.exists()) {
            if (dir.isDirectory) {
                val files = dir.listFiles()
                for (file in files) if (file.isFile) file.delete() else deleteDirectoryAllFile(
                    file
                )
                dir.delete()
            } else {
                dir.delete()
            }
        }
    }

    fun deleteDirectoryAllFile(dirPath: String?) {
        deleteDirectoryAllFile(File(dirPath))
    }

    @Throws(IOException::class)
    fun copyFile(fromPath: String?, toPath: String?): Boolean {
        if (fromPath.isNullOrBlank() || toPath.isNullOrBlank()) return false
        mkdirsParent(toPath)
        writeBytes(toPath, readByteArray(fromPath))
        return true
    }

    fun copyFile(from: File, to: File): Boolean {
        if (!from.exists()) return false
        mkdirsParent(from)
        writeBytes(to.absolutePath, readByteArray(from.absolutePath))
        return true
    }

    fun copyDirectory(fromPath: String?, toPath: String): Int {
        var counter = 0
        val files =
            scanDirectory(File(fromPath))
        for (file in files!!) {
            try {
                copyFile(
                    file.absolutePath,
                    file.absolutePath.replace(
                        File(fromPath).parentFile.absolutePath.toRegex(),
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
        if (fromPath.isBlank() || toPath.isBlank()) return false
        mkdirsParent(toPath)
        writeBytes(fromPath, readByteArray(toPath))
        deleteFile(fromPath)
        return true
    }

    fun moveDirectory(fromPath: String, toPath: String): Boolean {
        if (fromPath.isBlank() || toPath.isBlank()) return false
        copyDirectory(fromPath, toPath)
        deleteDirectoryAllFile(fromPath)
        return true
    }

    fun exists(path: String?): Boolean {
        return if (path == null) false else File(path).exists()
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
        } catch (e: IOException) { // 这里不作处理
        }
        return false
    }

    /**
     * 扫描目录
     */
    fun scanDirectory(
        rootPath: File,
        vararg suffixs: String,
        containDirectory: Boolean = false,
        filter: (file: File) -> Boolean = { true }
    ): List<File> {
        if (!rootPath.exists())
            return arrayListOf()
        val rootFiles =
            if (suffixs.size == 0) {
                rootPath.listFiles()
            } else {
                rootPath.listFiles(FileFilter { file ->
                    if (file.isDirectory)
                        return@FileFilter true
                    else
                        for (suffix in suffixs)
                            if (file.name.toLowerCase().endsWith(suffix.toLowerCase())) return@FileFilter true
                    return@FileFilter false
                })
            }
        rootFiles ?: return arrayListOf()
        val files: MutableList<File> = ArrayList(rootFiles.size)
        for (file in rootFiles) {
            if (file.isDirectory) {
                val fs = scanDirectory(file, suffixs = *suffixs, containDirectory = containDirectory, filter = filter)
                if (fs.isNotEmpty()) files.addAll(fs)
                if (containDirectory) files.add(file)
            } else {
                if (filter(file)) files.add(file)
            }
        }
        return files
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

//    fun getUrlFileName(url: String): String {
//        var filename = url.substring(url.lastIndexOf("/") + 1)
//        val endIndex = filename.indexOf("?")
//        if (endIndex != -1) filename = filename.substring(0, endIndex)
//        return filename
//    }

    fun getUrlFileName(_url: String): String {
        val url = URLDecoder.decode(_url, "utf-8")
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
        newName = if (end != -1) {
            oldName.substring(0, end) + append + oldName.substring(end)
        } else {
            file.name + append
        }
        return File(file.parent, newName)
    }

    fun getFileSuffix(filename: String): String {
        return if (filename.isBlank()) {
            ""
        } else {
            var index = filename.lastIndexOf('.')
            if (index != -1 && ++index < filename.length) filename.substring(index, filename.length) else ""
        }
    }

    fun getFileSuffix(file: File): String {
        return getFileSuffix(file.absolutePath)
    }

    fun getPureFilename(filename: String): String {
        return filename.replace("\\.\\.\\\\|\\\\|/|../".toRegex(), "-")
    }

    interface OnFileScanCallback {
        fun onScan(file: File?): Boolean
    }

    enum class FileSize(size: Long) {
        Byte(IOUtil.Byte.toLong()), KB(IOUtil.KB.toLong()), MB(IOUtil.MB.toLong()), GB(
            IOUtil.GB.toLong()
        ),
        TB(IOUtil.TB);

        private val size: Long = -1
        fun size(): Long {
            return size
        }

        override fun toString(): String {
            return name
        }
    }

    object ZipUtil {
        @JvmOverloads
        @Throws(IOException::class)
        fun createZip(directory: File, onCompressProgress: OnCompressProgress? = null): File {
            var zos: ZipOutputStream? = null
            var bis: BufferedInputStream? = null
            val zip = File("$directory.zip")
            try {
                zos = ZipOutputStream(FileOutputStream(zip))
                val fileList =
                    scanDirectory(directory)
                for (f in fileList) {
                    bis = BufferedInputStream(FileInputStream(f))
                    val entry = ZipEntry(f.absolutePath.replace(directory.absolutePath, ""))
                    zos.putNextEntry(entry)
                    val buff = ByteArray(BUFF_SIZE)
                    var length: Long = 0
                    var pos = 0
                    while (bis.read(buff).also { pos = it } > 0) {
                        zos.write(buff, 0, pos)
                        length += pos.toLong()
                        onCompressProgress?.onProgress(f, length)
                    }
                    bis.close()
                    zos.closeEntry()
                }
            } finally {
                bis?.close()
                zos?.close()
            }
            return zip
        }

        interface OnCompressProgress {
            fun onProgress(file: File?, length: Long): Boolean
        }
    }
}