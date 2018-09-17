package app

import activity.HomeActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context, intent: Intent)
    {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        val i = Intent(context, HomeActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }
}