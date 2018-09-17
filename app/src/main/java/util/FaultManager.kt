package util

import org.json.JSONArray
import org.json.JSONObject

class FaultManager
{
    /**
     * {
            "info": [
            {
                "goodsType": "1-1",
                "statusCode": 0
            },
            {
                "goodsType": "1-2",
                "statusCode": 1
            }
            ],
        "macAddr": "11:22:33:44:55;66",
        "trouble": false
        }

        {"macAddr  ": "","trouble":"false ","springGoodsType1":"true"}
     */

    companion object
    {
        val instance: FaultManager by lazy { FaultManager() }
    }


    fun start()
    {
        Task.DelayHandler.post(UpdateTask())
    }


    private class UpdateTask : Runnable
    {
        override fun run()
        {
            try {
                val ret = Http.postStatus()
                log(ret)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Task.DelayHandler.postDelayed(this, 3 * 60 * 1000)
        }

    }
}