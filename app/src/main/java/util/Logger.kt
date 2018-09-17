package util

import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object Logger
{
    private const val NAME = "弹簧机日志文件.txt"
    private var mFileWrite: FileWriter? = null
    private val mFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

    init {
        try {
            createLoggerFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createLoggerFile()
    {
        val file = File(Environment.getExternalStorageDirectory(), NAME)
        if (!file.exists()) {
            file.createNewFile()
        }
        mFileWrite = FileWriter(file, true)
    }

    fun println(msg: String)
    {
        if (mFileWrite == null) {
            createLoggerFile()
        }
        val date = mFormat.format(Calendar.getInstance().time)
        mFileWrite?.appendln(date)
        mFileWrite?.appendln(msg)
        mFileWrite?.appendln()
        mFileWrite?.flush()
    }
}