package popup

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import app.App
import com.hontech.springclient.R
import com.wang.avi.AVLoadingIndicatorView
import data.WaresManager
import util.Http
import util.Logger
import util.Task
import util.log

class PayPopupWindow
{
    companion object
    {
        private val TIME_OUT = 90
        private val WIDTH = App.context.resources.getDimension(R.dimen.x800).toInt()
        private val HEIGHT = App.context.resources.getDimension(R.dimen.y1200).toInt()
        val instance: PayPopupWindow by lazy { PayPopupWindow() }
    }

    private val mView = LayoutInflater.from(App.context).inflate(R.layout.popup_pay, null)
    private val mImageView = mView.findViewById<ImageView>(R.id.id_popup_pay_image_view)
    private val mLoading = mView.findViewById<AVLoadingIndicatorView>(R.id.id_popup_pay_loading)
    private val mTextViewName = mView.findViewById<TextView>(R.id.id_popup_pay_text_view_name)
    private val mButtonCancel = mView.findViewById<Button>(R.id.id_popup_pay_button_cancel)
    private val mPopupWindow = PopupWindow(mView, WIDTH, HEIGHT, true)
    private val mAnimator = ValueAnimator.ofInt(TIME_OUT, 0)

    private var mPaySuccessCallback = {}

    init
    {
        mAnimator.duration = TIME_OUT * 1000L
        mAnimator.interpolator = LinearInterpolator()

        mPopupWindow.isOutsideTouchable = false
        mPopupWindow.setOnDismissListener {
            mAnimator.cancel()
            mAnimator.removeAllUpdateListeners()
        }

        /**
         * 取消支付
         */
        mButtonCancel.setOnClickListener {

            Http.QueryPayTask.mFlag = false // 停止查询
            mPopupWindow.dismiss()
            Logger.println("用户取消支付")
        }
    }

    private fun startAnimator()
    {
        mAnimator.addUpdateListener {
            val intValue = it.animatedValue as Int
            mButtonCancel.text = "取消付款($intValue)"
            if (intValue <= 0) {
                onTimeOut()
            }
        }
        mAnimator.start()
    }

    /**
     * 支付超时
     */
    private fun onTimeOut()
    {
        Http.QueryPayTask.mFlag = false // 停止查询
        Logger.println("用户支付超时")
        mPopupWindow?.dismiss()
    }

    /**
     * 获取二维码失败
     */
    private fun onError()
    {
        mLoading.hide()
        mImageView.visibility = View.VISIBLE
        mImageView.setImageResource(R.drawable.ic_network_error)
        Http.QueryPayTask.mFlag = false
        Task.UiHandler.postDelayed({
            mPopupWindow.dismiss()
        }, 5000)
    }

    /**
     * 支付成功
     */
    private fun onSuccess()
    {
        log("开始出货")
        mPopupWindow.dismiss()
        mPaySuccessCallback()
    }

    private fun setQrCode(bitmap: Bitmap?)
    {
        if (bitmap == null) {
            onError()
            return
        }
        mLoading.hide()
        mImageView.visibility = View.VISIBLE
        mImageView.setImageBitmap(bitmap)
        Http.QueryPayTask.mFlag = true
        Task.AsyncHandler.post(Http.QueryPayTask(::onSuccess))
    }

    private fun setUi()
    {
        val info = WaresManager.instance.getSelectInfo()
        Logger.println("用户选择商品:" + info.message())
        mTextViewName.text = "${info.name} : ¥${info.price}"
        mImageView.visibility = View.GONE
        mLoading.visibility = View.VISIBLE
        mLoading.show()
        startAnimator()
    }


    fun show(view: View, onPaySuccess: () -> Unit)
    {
        mPopupWindow.dismiss()
        setUi()
        mPaySuccessCallback = onPaySuccess
        mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        Task.AsyncHandler.post {

            val bm = Http.acquireQrBitmap()
            Task.UiHandler.post {
                setQrCode(bm)
            }
        }
    }

    private constructor()
}