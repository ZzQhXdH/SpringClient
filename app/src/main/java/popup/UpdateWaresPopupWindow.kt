package popup


import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import app.App
import com.hontech.springclient.R
import com.wang.avi.AVLoadingIndicatorView

class UpdateWaresPopupWindow
{
    companion object
    {
        var DisplayCounter = 0
        val instance: UpdateWaresPopupWindow by lazy { UpdateWaresPopupWindow() }
        private val WIDTH = App.context.resources.getDimension(R.dimen.x600).toInt()
        private val HEIGHT = App.context.resources.getDimension(R.dimen.y800).toInt()
    }

    private val mView = LayoutInflater.from(App.context).inflate(R.layout.popup_update_wares, null)
    private val mLoading = mView.findViewById<AVLoadingIndicatorView>(R.id.id_popup_update_wares_loading)
    private val mTextViewHint = mView.findViewById<TextView>(R.id.id_popup_update_text_view_hint)
    private var mPopupWindow: PopupWindow? = null

    fun show(view: View, hint: String, dismiss: () -> Unit)
    {
        val group = mView.parent
        if (group != null) {
            (group as ViewGroup).removeAllViews()
        }
        DisplayCounter ++
        mLoading.show()
        mTextViewHint.text = hint
        mPopupWindow = PopupWindow(mView, WIDTH, HEIGHT, false)
        mPopupWindow!!.isOutsideTouchable = true
        mPopupWindow!!.setOnDismissListener(dismiss)
        mPopupWindow!!.showAtLocation(view, Gravity.CENTER, 0, 0)
    }

    fun dismiss()
    {
        mPopupWindow?.setOnDismissListener {  }
        mPopupWindow?.dismiss()
    }

}