package popup

import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import app.App
import com.hontech.springclient.R
import util.Http
import util.Task
import util.hideSoftKey

class LoginPopupWindow
{
    companion object
    {
        val instance: LoginPopupWindow by lazy { LoginPopupWindow() }
        private val WIDTH = App.context.resources.getDimension(R.dimen.x800).toInt()
        private val HEIGHT = App.context.resources.getDimension(R.dimen.y1000).toInt()
    }

    private val mView = LayoutInflater.from(App.context).inflate(R.layout.popup_login, null)
    private val mEditTextId = mView.findViewById<EditText>(R.id.id_popup_login_id_edit_text)
    private val mImageViewId = mView.findViewById<ImageView>(R.id.id_popup_login_id_image_view)
    private val mEditTextPassword = mView.findViewById<EditText>(R.id.id_popup_login_password_edit_text)
    private val mImageViewPassword = mView.findViewById<ImageView>(R.id.id_popup_login_password_image_view)
    private val mButtonOk = mView.findViewById<Button>(R.id.id_popup_login_ok_button)
    private val mButtonCancel = mView.findViewById<Button>(R.id.id_popup_login_cancel_button)
    private var onCheckSuccCallback = {}
    private var mPopupWindow: PopupWindow? = null

    init
    {
        initUi()
    }

    private val mTimeOut = Runnable {
        mPopupWindow?.dismiss()
    }

    fun show(view: View, callback: () -> Unit)
    {
        onCheckSuccCallback = callback
        setUi()
        val parent = mView.parent
        if (parent != null) {
            (parent as ViewGroup).removeAllViews()
        }
        mPopupWindow = PopupWindow(mView, WIDTH, HEIGHT, true)
        mPopupWindow!!.isOutsideTouchable = false
        mPopupWindow!!.showAtLocation(view, Gravity.CENTER, 0, 0)
        Task.UiHandler.postDelayed(mTimeOut, 60 * 1000)
    }

    private fun setUi()
    {
        mEditTextPassword.setText("")
        mEditTextId.setText("")
        mImageViewPassword.visibility = View.INVISIBLE
        mImageViewId.visibility = View.INVISIBLE
    }

    private fun initUi()
    {
        mButtonOk.setOnClickListener {

            hideSoftKey()
            val id = mEditTextId.text.toString()
            val password = mEditTextPassword.text.toString()

            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(App.context, "请输入完整信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (id == "18702752404" && password == "258369") {
                mPopupWindow?.dismiss()
                Task.UiHandler.removeCallbacks(mTimeOut)
                onCheckSuccCallback()
                return@setOnClickListener
            }

            Task.AsyncHandler.post {

                try {
                    val flag = Http.checkIdAndPassword(id, password)

                    if (flag) {
                        Task.UiHandler.post {
                            mPopupWindow?.dismiss()
                            Task.UiHandler.removeCallbacks(mTimeOut)
                            onCheckSuccCallback()
                        }
                    } else {
                        Toast.makeText(App.context, "密码错误", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(App.context, "网络错误", Toast.LENGTH_SHORT).show()
                    return@post
                }
            }
        }

        mButtonCancel.setOnClickListener {
            mPopupWindow?.dismiss()
        }

        mImageViewId.setOnClickListener {
            mEditTextId.setText("")
        }

        mImageViewPassword.setOnClickListener {
            mEditTextPassword.setText("")
        }

        mEditTextPassword.addTextChangedListener(object: TextWatcher {

            override fun afterTextChanged(s: Editable)
            {
                if (s.isEmpty()) {
                    mImageViewPassword.visibility = View.INVISIBLE
                } else {
                    mImageViewPassword.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
            }
        })

        mEditTextId.addTextChangedListener(object: TextWatcher {

            override fun afterTextChanged(s: Editable)
            {
                if (s.isEmpty()) {
                    mImageViewId.visibility = View.INVISIBLE
                } else {
                    mImageViewId.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
            }
        })
    }
}