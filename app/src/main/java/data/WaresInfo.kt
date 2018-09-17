package data



class WaresInfo(val name: String,
                val id: String,
                val price: String,
                val minImage: String,
                val maxImage: String,
                val goodsTypes: Array<GoodsTypeInfo?>)
{
    val sumNumber: Int

    init
    {
        var s = 0
        for (g in goodsTypes) {
            s += g!!.num
        }
        sumNumber = s
    }

    fun getGoodsTypeInfo(): GoodsTypeInfo?
    {
        for (type in goodsTypes)
        {
            if (type!!.num > 0) {
                return type
            }
        }
        return null
    }

    fun message(): String
    {
        val sb = StringBuilder("名称:$name")
        sb.append(",价格:$price")
        goodsTypes.forEachIndexed { index, goodsTypeInfo ->
            sb.append(",货道$index:${goodsTypeInfo!!.row}-${goodsTypeInfo!!.col}")
        }
        return sb.toString()
    }

}