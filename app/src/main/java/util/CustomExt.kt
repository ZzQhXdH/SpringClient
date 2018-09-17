package util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.*
import android.os.IBinder
import android.util.Log
import android.view.inputmethod.InputMethodManager
import app.App
import com.hontech.springclient.R

/**
 * 获取货道号
 */
fun ByteArray.getTypeId() = this[0].toInt()

/**
 * 获取货道剩余货的数量
 */
fun ByteArray.getWarersNumber() = this[1].toInt()

/**
 * 获取故障信息
 */
fun ByteArray.getFault() = this[2].toInt()

fun ByteArray.isSuccess() = this[1].toInt() != 0x00

fun ByteArray.toHexString(): String
{
    val sb = StringBuilder()
    this.forEach {
        sb.append(String.format("%02x ", it))
    }
    return sb.toString()
}

fun ByteArray.toMacString(): String
{
    val sb = StringBuilder()
    for ((index, value) in this.withIndex())
    {
        if (index == (this.size - 1)) {
            sb.append(String.format("%02x", value))
        } else {
            sb.append(String.format("%02x:", value))
        }
    }
    return sb.toString()
}

inline fun log(msg: String, tag: String = "调试")
{
    Log.d(tag, msg)
}

fun resetSystem()
{
    val proc = Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot "))
    proc.waitFor()
}

fun resetApp()
{
    val i = App.context.packageManager.getLaunchIntentForPackage(App.context.packageName)
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    App.context.startActivity(i)
}

fun hideSoftKey()
{
    val imm = App.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
}


fun xCreateRoundedBitmap(bitmap: Bitmap, radius: Float): Bitmap
{
    val bm = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bm)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.xfermode = null
    val rectF = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
    canvas.drawRoundRect(rectF, radius, radius, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    return bm
}

fun xCreateRoundStateDrawable(bitmap: Bitmap, radius: Float): Drawable
{
    val rawDrawable = BitmapDrawable(xCreateRoundedBitmap(bitmap, radius))
    val stateListDrawable = StateListDrawable()
    stateListDrawable.addState(intArrayOf(-android.R.attr.state_pressed), rawDrawable)
    val gradientDrawable = GradientDrawable()
    gradientDrawable.cornerRadius = radius
    gradientDrawable.setColor(App.context.resources.getColor(R.color.colorShadow))
    val layerDrawable = LayerDrawable(arrayOf(rawDrawable, gradientDrawable))
    stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), layerDrawable)
    return stateListDrawable
}
