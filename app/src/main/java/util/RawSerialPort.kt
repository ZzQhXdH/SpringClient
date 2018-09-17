package util

import android_serialport_api.SerialPort
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object RawSerialPort
{
    private const val PATH = "/dev/ttyS3"
    private var mSerialPort: SerialPort? = null
    private var mInput: InputStream? = null
    private var mOutput: OutputStream? = null

    fun xOpen(): Boolean
    {
        val file = File(PATH)
        try {
            mSerialPort = SerialPort(file, 9600, 0)
        } catch (e: Exception){
            e.printStackTrace()
            return false
        }
        mInput = mSerialPort !!.inputStream
        mOutput = mSerialPort !!.outputStream
        return true
    }

    fun write(byteArray: ByteArray)
    {
        log(byteArray.toHexString(), "串口发送")
        mOutput?.write(byteArray)
    }

    fun read(byteArray: ByteArray) = mInput?.read(byteArray) ?: 0

    fun write(wData: ByteArray, rData: ByteArray, hopeSize: Int, timeOut: Int): Boolean
    {
        val msg = wData.toHexString()
        log(msg, "串口发送")
        Logger.println("串口发送:$msg")

        mOutput?.write(wData)
        var size = hopeSize
        var ret = 0
        var counter = 0

        while (true)
        {
            ret = mInput?.available() ?: 0
            if (ret >= size) {
                mInput?.read(rData)
                return true
            }
            Thread.sleep(20)
            counter += 20
            if (counter >= timeOut) {
                return false
            }
        }
    }

    fun writeShipment(id: Int, timeOut: Int): Boolean
    {
        val byteArray = byteArrayOf(
                                0x1b.toByte(),
                                0x48.toByte(),
                                id.toByte(),
                                0x0d.toByte(),
                                0x0a.toByte())
        val bytes = ByteArray(3)
        val ret = write(byteArray, bytes, 3, timeOut)
        if (!ret) {
            return false
        }
        if (bytes[0].toInt() == id && (bytes[1].toInt() != 0x00)) {
            return true
        }
        return false
    }

    fun setGoodsTypeNumber(id: Int, num: Int): Boolean
    {
        val byteArray = byteArrayOf(0x1b, 0x50, id.toByte(), num.toByte(), 0x0d, 0x0a)
        val bytes = ByteArray(3)
        val ret = write(byteArray, bytes, 2, 1000)
        if (ret && bytes[0] != 0x45.toByte()) {
            return true
        }
        return false
    }

    fun resetGoodsType(): Boolean
    {
        val byteArray = byteArrayOf(0x1b, 0x47, 0x00, 0x0d, 0x0a)
        val bytes = ByteArray(2)
        val ret = write(byteArray, bytes, 2, 1000)
        return ret && (bytes[0] == 0x00.toByte() && bytes[1] == 0x01.toByte())
    }

    fun testMoto(id: Int): Boolean
    {
        val byteArray = byteArrayOf(0x1b, 0x49, id.toByte(), 0x0d, 0x0a)
        val bytes = ByteArray(2)
        val ret = write(byteArray, bytes, 2, 5000)
        if (ret && (bytes[0].toInt() == id && bytes[1].toInt() == 0x00 )) {
            return true
        }
        return false
    }

    fun queryStatus(id: Int): Int
    {
        val byteArray = byteArrayOf(0x1b, 0x46, id.toByte(), 0x0d, 0x0a)
        val bytes = ByteArray(3)
        val ret = write(byteArray, bytes, 3, 5000)
        if (!ret) {
            return 0x06
        }
        if (bytes[2] == 0xFF.toByte()) {
            return 5
        }
        return bytes[2].toInt()
    }

}