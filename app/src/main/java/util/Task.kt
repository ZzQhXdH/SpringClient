package util

import android.os.Handler
import android.os.HandlerThread

object Task
{
    private val AsyncTask = HandlerThread("Async")
    private val DelayTask = HandlerThread("Delay")
    val UiHandler: Handler
    val AsyncHandler: Handler
    val DelayHandler: Handler

    init
    {
        AsyncTask.start()
        DelayTask.start()
        UiHandler = Handler()
        AsyncHandler = Handler(AsyncTask.looper)
        DelayHandler = Handler(DelayTask.looper)
    }
}


