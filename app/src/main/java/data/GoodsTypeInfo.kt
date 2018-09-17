package data

class GoodsTypeInfo(val row: Int, val col: Int, var num: Int)
{
    val rawNumber = num

    fun getId(): Int
    {
        return (row - 1) * 10 + col
    }

    override fun toString(): String
    {
        return "$row-$col"
    }
}