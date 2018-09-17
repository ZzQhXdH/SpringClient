package activity


import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import app.App
import app.sendAction
import com.hontech.springclient.R
import data.WaresManager
import popup.WaitPopupWindow

import util.RawSerialPort
import util.Task


class DebugActivity : AppCompatActivity()
{
    private val mRecyclerView: RecyclerView by lazy { findViewById<RecyclerView>(R.id.id_debug_recycler_view) }
    private val mButtonReset: Button by lazy { findViewById<Button>(R.id.id_debug_button_reset_goods_type) }
    private val mButtonSetting: Button by lazy { findViewById<Button>(R.id.id_debug_button_setting_goods_type) }
    private val mButtonAcquireGoodsType: Button by lazy { findViewById<Button>(R.id.id_debug_button_acquire_goods_type) }
    private val mButtonGoodsTypeFinish: Button by lazy { findViewById<Button>(R.id.id_debug_button_goods_type_finish) }
    private val mPickerRow: NumberPicker by lazy { findViewById<NumberPicker>(R.id.id_debug_number_picker_row) }
    private val mPickerCol: NumberPicker by lazy { findViewById<NumberPicker>(R.id.id_debug_number_picker_col) }
    private val mButtonTest: Button by lazy { findViewById<Button>(R.id.id_debug_button_test) }
    private lateinit var mAdapter: RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        initUi()
    }

    private fun initUi()
    {
        mButtonReset.setOnClickListener(::onResetGoodsType)
        mButtonSetting.setOnClickListener(::onSettingGoodsType)
        mButtonAcquireGoodsType.setOnClickListener(::onAcquireGoodsType)
        mButtonGoodsTypeFinish.setOnClickListener(::onGoodsTypeFinish)
        mButtonTest.setOnClickListener(::onTestMoto)
        mPickerCol.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        mPickerRow.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        mPickerRow.minValue = 1
        mPickerRow.maxValue = 6
        mPickerCol.minValue = 1
        mPickerCol.maxValue = 8
        mAdapter = RecyclerViewAdapter()
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.addItemDecoration(RecyclerViewItemDecoration())
    }

    /**
     * 测试电机
     */
    private fun onTestMoto(view: View)
    {
        val row = mPickerRow.value
        val col = mPickerCol.value
        val id = (row - 1) * 10 + col

        Task.DelayHandler.post {

            val ret = RawSerialPort.testMoto(id)
            Task.UiHandler.post {

                if (ret) {
                    Toast.makeText(this@DebugActivity, "电机测试成功($row-$col)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DebugActivity, "电机测试失败($row-$col)", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    /**
     * 复位货道
     */
    private fun onResetGoodsType(view: View)
    {
        WaitPopupWindow.instance.show(mButtonReset) {}
        Task.DelayHandler.post {
            val ret = RawSerialPort.resetGoodsType()
            Task.UiHandler.post {
                WaitPopupWindow.instance.dismiss()
                if (ret) {
                    Toast.makeText(this@DebugActivity, "复位成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DebugActivity, "复位失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 设置货道
     */
    private fun onSettingGoodsType(view: View)
    {
        WaitPopupWindow.instance.show(mButtonReset) {}
        Task.DelayHandler.post(SettingGoodsTypeTask{
            WaitPopupWindow.instance.dismiss()
            if (it) {
                Toast.makeText(this@DebugActivity, "设置成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@DebugActivity, "设置失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu_debug, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.menu_debug_quit -> {
                finish()
            }

            R.id.id_debug_menu_quit_app -> {
                val i = Intent(this, HomeActivity::class.java)
                i.putExtra(HomeActivity.EXTRA_KEY_QUIT, true)
                startActivity(i)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 创建补货清单
     */
    private fun onAcquireGoodsType(view: View)
    {
        Task.DelayHandler.post {

            val ret = WaresManager.instance.updateReplenish()
            Task.UiHandler.post {
                if (ret) {
                    Toast.makeText(this@DebugActivity, "获取补货清单成功", Toast.LENGTH_SHORT).show()
                    mAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@DebugActivity, "获取补货清单失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 补货完成
     */
    private fun onGoodsTypeFinish(view: View)
    {
        Task.DelayHandler.post {

            val ret = WaresManager.instance.replenishFinish()
            Task.UiHandler.post {
                if (ret) {
                    Task.AsyncHandler.post {

                        while (!WaresManager.instance.updateWares()) {}
                        LocalBroadcastManager.getInstance(App.context).sendAction(App.ACTION_GET_WARES)
                       // LocalBroadcastManager.getInstance(App.context).sendAction(HomeActivity.ACTION_ACQUIRE_WARES)

                        Task.UiHandler.post {
                            Toast.makeText(this@DebugActivity, "补货完成成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@DebugActivity, "补货完成失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}

private class RecyclerViewItemDecoration : RecyclerView.ItemDecoration()
{
    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView?)
    {
        super.getItemOffsets(outRect, itemPosition, parent)
        outRect.top = 10
    }
}

private class RecyclerViewItem(itemView: View) : RecyclerView.ViewHolder(itemView)
{
    private val mTextViewName = itemView.findViewById<TextView>(R.id.id_item_debug_recycler_view_text_view_name)
    private val mTextViewNumber = itemView.findViewById<TextView>(R.id.id_item_debug_recycler_view_text_view_number)
    private val mButtonAdd = itemView.findViewById<Button>(R.id.id_item_debug_recycler_view_button_add)
    private val mButtonSub = itemView.findViewById<Button>(R.id.id_item_debug_recycler_view_button_sub)

    fun set(position: Int)
    {
        val info = WaresManager.instance.getReplenishInfoBR(position)
        mTextViewName.text = "${info.name}:${info.goodsTypeInfo}"
        mTextViewNumber.text = "应补:${info.goodsTypeInfo.rawNumber}\r\n实补:${info.goodsTypeInfo.num}"
        mButtonAdd.setOnClickListener {

            if (info.goodsTypeInfo.num < info.goodsTypeInfo.rawNumber) {
                info.goodsTypeInfo.num ++
                mTextViewNumber.text = "应补:${info.goodsTypeInfo.rawNumber}\r\n实补:${info.goodsTypeInfo.num}"
            }
        }
        mButtonSub.setOnClickListener {

            if (info.goodsTypeInfo.num > 0) {
                info.goodsTypeInfo.num --
                mTextViewNumber.text = "应补:${info.goodsTypeInfo.rawNumber}\r\n实补:${info.goodsTypeInfo.num}"
            }
        }
    }
}

private class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewItem>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewItem
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_debug_recycler, parent, false)
        return RecyclerViewItem(view)
    }

    override fun getItemCount() = WaresManager.instance.getReplenishInfoBRNumber()

    override fun onBindViewHolder(holder: RecyclerViewItem, position: Int)
    {
        holder.set(position)
    }
}

private class SettingGoodsTypeTask(val resultCallback: (status: Boolean) -> Unit) : Runnable
{
    private var mId = 0x01

    override fun run()
    {
        val ret = RawSerialPort.setGoodsTypeNumber(mId, 0x10)

        if (!ret) {
            Task.UiHandler.post {
                resultCallback(false)
            }
            return
        }

        if (mId >= 0x3C) {
            Task.UiHandler.post {
                resultCallback(true)
            }
            return
        }
        mId ++
        Task.AsyncHandler.post(this)
    }
}
