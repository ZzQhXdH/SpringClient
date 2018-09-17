package data

import app.App
import org.json.JSONArray
import org.json.JSONObject
import util.Http
import util.log

class WaresManager
{
    companion object
    {
        val instance: WaresManager by lazy { WaresManager() }
    }

    private val mWaresInfoList = arrayListOf<WaresInfo>()
    private val mReplenishInfoListBR = arrayListOf<ReplenishInfo>() // 补入的补货清单

    var Order = ""
    var SelectIndex = 0

    fun getWaresNumber() = mWaresInfoList.size

    fun getSelectInfo() = mWaresInfoList[SelectIndex]

    fun get(index: Int) = mWaresInfoList[index]

    fun getReplenishInfoBR(index: Int) = mReplenishInfoListBR[index]

    fun getReplenishInfoBRNumber() = mReplenishInfoListBR.size

    private fun parseGoodsType(goodsType: String, number: Int): GoodsTypeInfo
    {
        val list = goodsType.split("-")
        val row = list[0].toInt()
        val col = list[1].toInt()
        return GoodsTypeInfo(row, col, number)
    }

    private fun parseReplenishInfo(jsonObject: JSONObject): ReplenishInfo
    {
        val goodsType = jsonObject.optString("cargoData")
        val number = jsonObject.optInt("isExist")
        val name = jsonObject.optString("goodsName")
        return ReplenishInfo(parseGoodsType(goodsType, number), name)
    }

    private fun parseReplenishBR(jsonArray: JSONArray)
    {
        mReplenishInfoListBR.clear()
        val length = jsonArray.length()
        for (index in 0 until length)
        {
            mReplenishInfoListBR.add(parseReplenishInfo(jsonArray.getJSONObject(index)))
        }
    }

    fun updateReplenish(): Boolean
    {
        try {
            val result = Http.acquireGoodsType()
            log(result)
            val jsonObject = JSONObject(result)
            val jsonArray = jsonObject.optJSONArray("dataBR")
            parseReplenishBR(jsonArray)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        log(mReplenishInfoListBR.toString())
        return true
    }

    private fun toReplenishJson(index: Int): JSONObject
    {
        val info = mReplenishInfoListBR[index]
        val jsonObject = JSONObject()
        jsonObject.put("goodsName", info.name)
        jsonObject.put("cargoData", info.goodsTypeInfo.toString())
        jsonObject.put("isExist", info.goodsTypeInfo.num)
        return jsonObject
    }

    fun replenishFinish(): Boolean
    {
        try {
            val jsonObject = JSONObject()
            val jsonArray = JSONArray()
            val len = mReplenishInfoListBR.size
            for (index in 0 until len) {
                jsonArray.put(toReplenishJson(index))
            }
            jsonObject.put("replenInfos", jsonArray)
            jsonObject.put("macAddr", App.MacAddress)
            val content = jsonObject.toString()
            log(content)
            val result = Http.replenishGoodsTypeFinish(content)
            log(result)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun parseWaresGoodsType(jsonObject: JSONObject): GoodsTypeInfo
    {
        val goodsType = jsonObject.optString("cargoData")
        val number = jsonObject.optInt("goodsNum")
        val list = goodsType.split("-")
        val row = list[0].toInt()
        val col = list[1].toInt()
        return GoodsTypeInfo(row, col, number)
    }

    private fun parseWares(jsonObject: JSONObject): WaresInfo
    {
        val name = jsonObject.optString("WaresName")
        val id = jsonObject.optString("WaresId")
        val price = jsonObject.optString("WaresPrice")
        val image1 = jsonObject.optString("WaresImage1")
        val image2 = jsonObject.optString("WaresImage2")
        val jsonArray = jsonObject.optJSONArray("GoodsType")
        val len = jsonArray.length()
        val arr = arrayOfNulls<GoodsTypeInfo>(len)
        log(jsonArray.toString())
        for (index in 0 until len)
        {
            arr[index] = parseWaresGoodsType(jsonArray.getJSONObject(index))
        }
        return WaresInfo(name, id, price, image1, image2, arr)
    }

    fun updateWares(): Boolean
    {
        mWaresInfoList.clear()
        try {
            val result = Http.acquireWaresData()
            log(result)
            val jsonObject = JSONObject(result)
            val jsonArray = jsonObject.optJSONArray("arr")
            val len = jsonArray.length()
            for (index in 0 until len)
            {
                mWaresInfoList.add(parseWares(jsonArray.getJSONObject(index)))
            }
            log("WaresNumber=$len")
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 扣减库存
     */
    fun reportGoodsType(): Boolean
    {
        val info = getSelectInfo().getGoodsTypeInfo()!!
        for (i in 0 until 10)
        {
            try {
                Http.reportRepetroy(info)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    private constructor()
}