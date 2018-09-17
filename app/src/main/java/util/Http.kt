package util

import android.graphics.Bitmap
import android.widget.Toast
import app.App
import data.GoodsTypeInfo
import data.WaresManager
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object Http
{
    private const val BASE_URL = "http://test.hontech-rdcenter.com:8080" // 域名

    private const val WARES_URL = "$BASE_URL/bg-uc/goodssearch/goods-info/list.json" // 库存接口
    private const val REPLENISH_CHECK_URL = "$BASE_URL/bg-uc/replenishment/detail-inter/data.json" // 补货清单接口
    private const val REPLENISH_FINISH_URL = "$BASE_URL/bg-uc/replenishment/client/replen-data/finish.json" // 补货完成接口
    private const val WARES_SUB_URL = "$BASE_URL/bg-uc/replenishment/work-off/quantity.json" // 库存扣减接口
    private const val CHECK_ID_PASSWORD_URL = "$BASE_URL/bg-uc/checkMain/main-info/check.json" // 校验用户

    private const val STATUS_URL = "$BASE_URL/bg-uc/sbzt/receiveth1.json"

    private const val QRCODE_URL = "$BASE_URL/bg-uc/jf/com/pm/getICBCLink.json" // 二维码接口
    private const val QUERY_PAY_URL = "$BASE_URL/bg-uc/jf/com/pm/searchNotifyICBC.json" // 支付结果查询接口
    private const val REFUND_URL = "$BASE_URL/bg-uc/jf/com/pm/returnRefundICBC.json" // 退款接口

    private val mOkHttpClient = OkHttpClient.Builder()
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

    private val mMediaType = MediaType.parse("application/json; charset=utf-8")

    private inline fun post(content: String, url: String): String
    {
        val body = RequestBody.create(mMediaType, content)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        return mOkHttpClient.newCall(request).execute().body()?.string() ?: "net work error"
    }

    fun postStatus(): String
    {
        val json = JSONObject()
        json.put("macAddr", App.MacAddress)
        json.put("trouble", "false")
        json.put("springGoodsType1", "true")
        val content = json.toString()
        log(content)
        Logger.println(content)
        return post(content, STATUS_URL)
    }

    /**
     * 从服务器获取库存
     */
    fun acquireWaresData(): String
    {
        val jsonObject = JSONObject()
        jsonObject.put("macAddr", App.MacAddress)
        val content = jsonObject.toString()
        log("库存数据:" + content)
        Logger.println(content)
        return post(content, WARES_URL)
    }

    /**
     * 创建补货清单
     */
    fun acquireGoodsType(): String
    {
        val jsonObject = JSONObject()
        jsonObject.put("macAddr", App.MacAddress)
        val content = jsonObject.toString()
        log("创建补货清单:" + content)
        Logger.println(content)
        return post(content, REPLENISH_CHECK_URL)
    }

    /**
     * 补货完成
     */
    fun replenishGoodsTypeFinish(content: String): String
    {
        log(content)
        Logger.println("补货完成:" + content)
        return post(content, REPLENISH_FINISH_URL)
    }

    fun checkIdAndPassword(id: String, password: String): Boolean
    {
        val jsonObject = JSONObject()
        jsonObject.put("emplCode", id)
        jsonObject.put("password", password)
        jsonObject.put("macAddr", App.MacAddress)

        val result = post(jsonObject.toString(), CHECK_ID_PASSWORD_URL)
        log(result)

        val json = JSONObject(result)
        return json.optBoolean("success")
    }

    /**
     * 扣减库存
     */
    fun reportRepetroy(info: GoodsTypeInfo): String
    {
        val jsonObject = JSONObject()
        jsonObject.put("macAddr", App.MacAddress)
        jsonObject.put("cargoData",  info.toString())
        jsonObject.put("out_trade_no", WaresManager.instance.Order)
        log(jsonObject.toString())
        val res = post(jsonObject.toString(), WARES_SUB_URL)
        log(res)
        Logger.println("扣减库存:" + res)
        return res
    }

    fun acquireQrCode(): String
    {
        val jsonObject = JSONObject()
        val info = WaresManager.instance.getSelectInfo()
        jsonObject.put("macAddress", App.MacAddress)
        jsonObject.put("tradename", info.name)
        jsonObject.put("price", info.price)
        jsonObject.put("ID", info.id)
        val content = jsonObject.toString()
        log(content)
        Logger.println(content)
        val body = FormBody.Builder().add("goods", content).build()
        val request = Request.Builder().post(body).url(QRCODE_URL).build()
        val ret = mOkHttpClient.newCall(request).execute().body()?.string() ?: ""
        log(ret)
        Logger.println(ret)
        val json = JSONObject(ret)
        val qrCode = json.optString("icbc")
        WaresManager.instance.Order = json.optString("order")
        return qrCode
    }

    fun acquireQrBitmap(): Bitmap?
    {
        try {
            val ret = acquireQrCode()
            return QRCodeUtil.create(ret)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun queryPayResult(): Boolean
    {
        val order = WaresManager.instance.Order
        val body = FormBody.Builder()
                .add("macaddress", App.MacAddress)
                .add("out_trade_no", order)
                .build()
        val request = Request.Builder().url(QUERY_PAY_URL).post(body).build()
        val ret = mOkHttpClient.newCall(request).execute().body()?.string() ?: ""
        log(ret)
        Logger.println("支付结果:" + ret)
        val json = JSONObject(ret)
        return json.optString("result") == "success"
    }

    fun refundOfOrder(order: String, goodType: String)
    {
        Logger.println("开始退款-(3):$order")
        val body = FormBody.Builder()
                .add("macAddr", App.MacAddress)
                .add("refund_remark", "支付异常退款")
                .add("cargoData", goodType)
                .add("out_trade_no", order)
                .build()
        val request = Request.Builder().post(body).url(REFUND_URL).build()
        val ret = mOkHttpClient.newCall(request).execute().body()?.string() ?: ""
        log(ret)
        Logger.println(ret)
    }

    fun refund(): Boolean
    {
        val info = WaresManager.instance.getSelectInfo()
        Logger.println("开始退款:" + info.message())
        val body = FormBody.Builder()
                .add("macAddr", App.MacAddress)
                .add("refund_remark", "出货失败退款")
                .add("cargoData", info.getGoodsTypeInfo()!!.toString())
                .add("out_trade_no", WaresManager.instance.Order)
                .build()
        val request = Request.Builder().post(body).url(REFUND_URL).build()
        val ret = mOkHttpClient.newCall(request).execute().body()?.string() ?: ""
        log(ret)
        Logger.println(ret)
        return true
    }

    class QueryPayTask(private val notify: () -> Unit) : Runnable
    {
        companion object
        {
            var mFlag = true
        }

        override fun run()
        {
            if (!mFlag) {
                log("停止查询")
                return
            }

            try {
                val ret = Http.queryPayResult()
                if (ret) {
                    Task.UiHandler.post(notify)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            log("没有支付")
            Task.AsyncHandler.postDelayed(this, 1000)
        }
    }

}