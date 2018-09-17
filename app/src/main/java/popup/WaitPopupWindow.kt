package popup

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import app.App
import com.hontech.springclient.R
import com.wang.avi.AVLoadingIndicatorView

class WaitPopupWindow
{
    companion object
    {
        val instance: WaitPopupWindow by lazy { WaitPopupWindow() }
        private val WIDTH = App.context.resources.getDimension(R.dimen.x500).toInt()
        private val HEIGHT = App.context.resources.getDimension(R.dimen.y500).toInt()
    }

    private val mView = LayoutInflater.from(App.context).inflate(R.layout.popup_wait, null)
    private val mLoading = mView.findViewById<AVLoadingIndicatorView>(R.id.id_popup_wait_loading)
    private var mPopupWindow: PopupWindow? = null

    fun dismiss() {
        mPopupWindow?.setOnDismissListener {  }
        mPopupWindow?.dismiss()
    }

    fun show(view: View, listener: () -> Unit)
    {
        val group = mView.parent
        if (group != null) {
            (group as ViewGroup).removeAllViews()
        }
        mLoading.show()
        mPopupWindow = PopupWindow(mView, WIDTH, HEIGHT, true)
        mPopupWindow!!.setOnDismissListener(listener)
        mPopupWindow!!.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    private constructor()
}