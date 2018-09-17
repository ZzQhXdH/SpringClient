package util

import android.graphics.Bitmap
import app.App
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.hontech.springclient.R


object QRCodeUtil
{
    private val WIDTH = App.context.resources.getDimension(R.dimen.x400).toInt()
    private val HEIGHT = App.context.resources.getDimension(R.dimen.y400).toInt()
    private val mQrCodeWrite = QRCodeWriter()
    private val mHintTypeMap = HashMap<EncodeHintType, Any>()

    init
    {
        mHintTypeMap.put(EncodeHintType.CHARACTER_SET, "utf-8")
        mHintTypeMap.put(EncodeHintType.MARGIN, 1)
        mHintTypeMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
    }

    fun create(content: String, width: Int = WIDTH, height: Int = HEIGHT): Bitmap
    {
        val matrix = mQrCodeWrite.encode(content, BarcodeFormat.QR_CODE, width, height, mHintTypeMap)
        val pixel = IntArray(width * height)
        for (i in 0 until height)
        {
            for (j in 0 until width)
            {
                if (matrix.get(j, i)) {
                    pixel[i * width + j] = 0x00
                } else {
                    pixel[i * width + j] = 0xFFFFFFFF.toInt()
                }
            }
        }
        return Bitmap.createBitmap(pixel, width, height, Bitmap.Config.RGB_565)
    }
}