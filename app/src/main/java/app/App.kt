package app

import activity.HomeActivity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager

import com.hontech.springclient.R
import util.*
import java.net.NetworkInterface

class App : Application()
{
    companion object
    {
        const val ACTION_GET_WARES = "action.get.wares"

        lateinit var context: Context
            private set

        var ResetFlag = true

        var WIDTH = 0
            private set

        var HEIGHT = 0
            private set

        const val ITEM_H_COUNTER = 2
        const val ITEM_V_COUNTER = 3
        const val ITEM_COUNTER = ITEM_H_COUNTER * ITEM_V_COUNTER

        val MacAddress = getLocalEthernetMacAddress()
    }

    private fun onException(thread: Thread, throwable: Throwable)
    {
        throwable.printStackTrace()
        resetApp()
    }

    override fun onCreate()
    {
        super.onCreate()
        context = applicationContext
        Thread.setDefaultUncaughtExceptionHandler(::onException)
        WIDTH = context.resources.getDimension(R.dimen.x1080).toInt()
        HEIGHT = context.resources.getDimension(R.dimen.y1920).toInt()
        init()
        FaultManager.instance.start()
        log(MacAddress)
    }

    private fun init()
    {
        val ret = RawSerialPort.xOpen()
        if (ret) {
            log("串口打开成功")
        } else {
            log("串口打开失败")
        }
    }

}

fun BroadcastReceiver.register(vararg actions: String)
{
    val filter = IntentFilter()
    for (action in actions) {
        filter.addAction(action)
    }
    LocalBroadcastManager.getInstance(App.context).registerReceiver(this, filter)
}

fun BroadcastReceiver.unregister()
{
    LocalBroadcastManager.getInstance(App.context).unregisterReceiver(this)
}

fun LocalBroadcastManager.sendAction(action: String)
{
    val intent = Intent(action)
    this.sendBroadcast(intent)
}

private fun getLocalEthernetMacAddress(): String
{
    try {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        for (inter in networkInterfaces)
        {
            if (inter.name == "eth0") {
                return inter.hardwareAddress.toMacString()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun resetApp()
{
    val i = Intent(App.context, HomeActivity::class.java)
    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    val pi = PendingIntent.getActivity(App.context, 0, i, 0)
    val am = App.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val info = AlarmManager.AlarmClockInfo(System.currentTimeMillis() + 500, pi)
    am.setAlarmClock(info, pi)

    android.os.Process.killProcess(android.os.Process.myPid())
}








































