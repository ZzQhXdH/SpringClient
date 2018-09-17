package data

class ReplenishInfo(val goodsTypeInfo: GoodsTypeInfo, val name: String)
{
    override fun toString(): String
    {
        return "$name:$goodsTypeInfo"
    }
}