package popup

import activity.HomeActivity
import android.animation.ValueAnimator
import android.support.v4.content.LocalBroadcastManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import app.App
import app.sendAction
import com.hontech.springclient.R
import com.wang.avi.AVLoadingIndicatorView
import data.WaresInfo
import data.WaresManager
import util.*

class DeliverPopupWindow
{
    companion object
    {
        val instance: DeliverPopupWindow by lazy { DeliverPopupWindow() }
        private val WIDTH = App.context.resources.getDimension(R.dimen.x800).toInt()
        private val HEIGHT = App.context.resources.getDimension(R.dimen.y1200).toInt()
        private const val MAX_COUNT = 60
    }

    private val mView = LayoutInflater.from(App.context).inflate(R.layout.popup_deliver, null)
    private val mLoading = mView.findViewById<AVLoadingIndicatorView>(R.id.id_popup_deliver_loading)
    private val mTextViewHint = mView.findViewById<TextView>(R.id.id_popup_deliver_text_view_hint)
    private val mTextViewCounter = mView.findViewById<TextView>(R.id.id_popup_deliver_image_view_counter)
    private val mPopupWindow = PopupWindow(mView, WIDTH, HEIGHT, true)
    private val mAnimator = ValueAnimator.ofInt(MAX_COUNT, 0)

    init
    {
        mAnimator.duration = MAX_COUNT * 1000L
        mAnimator.interpolator = LinearInterpolator()
        mPopupWindow.isOutsideTouchable = false
    }

    private val deliverTask = object: Runnable
    {
        override fun run()
        {
            val info = WaresManager.instance.getSelectInfo()
            val ret = RawSerialPort.writeShipment(info.getGoodsTypeInfo()!!.getId(),
                    (MAX_COUNT - 10) * 1000)
            Task.UiHandler.post {
                if (ret) {
                    onSuccess()
                } else {
                    onError()
                }
            }
        }
    }

    private fun startShipment()
    {
        Task.DelayHandler.removeCallbacks(deliverTask)
        Task.DelayHandler.post(deliverTask)
    }

    private fun setUi()
    {
        val info = WaresManager.instance.getSelectInfo()
        mLoading.show()
        mTextViewHint.text = "${info.name}正在出货..."
        startShipment()
        startAnimator()
    }

    private fun startAnimator()
    {
        mAnimator.removeAllUpdateListeners()
        mAnimator.addUpdateListener {
            val count = it.animatedValue as Int
            mTextViewCounter.text = count.toString()
            if (count <= 0) {
                onTimeOut()
            }
        }
        mAnimator.start()
    }

    private fun onTimeOut()
    {
        Logger.println("出货超时")
        mAnimator.removeAllUpdateListeners()
        mAnimator.cancel()
        mTextViewHint.text = "出货超时!"
        Task.AsyncHandler.post {
            Http.refund()
        }
        Task.UiHandler.postDelayed({
            mPopupWindow.dismiss()
        }, 2000)
    }

    private fun onError()
    {
        Logger.println("出货失败")
        mAnimator.removeAllUpdateListeners()
        mAnimator.cancel()
        mTextViewHint.text = "出货失败!"
        Task.AsyncHandler.post {
            Http.refund()
        }
        Task.UiHandler.postDelayed({
            mPopupWindow.dismiss()
        }, 5000)
    }

    private fun onSuccess()
    {
        Logger.println("出货成功")
        mAnimator.removeAllUpdateListeners()
        mAnimator.cancel()
        mTextViewHint.text = "出货成功!"

        Task.AsyncHandler.post {

            while (!WaresManager.instance.reportGoodsType()) {}
            while (!WaresManager.instance.updateWares()) {}
            LocalBroadcastManager.getInstance(App.context).sendAction(App.ACTION_GET_WARES)

            Task.UiHandler.postDelayed({
                mPopupWindow.dismiss()
            }, 2000)
        }
    }

    fun show(view: View)
    {
        setUi()
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    private constructor()
}