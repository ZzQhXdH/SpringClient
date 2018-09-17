package activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.hontech.springclient.R
import data.WaresManager
import fragment.MenuFragment
import popup.LoginPopupWindow
import popup.UpdateWaresPopupWindow
import util.Task
import android.os.Build
import android.support.v4.view.PagerAdapter
import app.*


class HomeActivity : AppCompatActivity()
{
    companion object
    {
        const val EXTRA_KEY_QUIT = "extra.key.quit"
        const val ACTION_ACQUIRE_WARES = "action.acquire.wares"
    }

    private val mViewPager: ViewPager by lazy { findViewById<ViewPager>(R.id.id_home_view_pager) }
    private val mButtonUp: Button by lazy { findViewById<Button>(R.id.id_home_button_up) }
    private val mButtonDown: Button by lazy { findViewById<Button>(R.id.id_home_button_down) }
    private val mTextViewDebug: TextView by lazy { findViewById<TextView>(R.id.id_home_text_view_debug) }
    private var mAdapter: ViewPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUi()
        mWaresUpdateReceiver.register(App.ACTION_GET_WARES)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean)
    {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && (UpdateWaresPopupWindow.DisplayCounter == 0)) {
            onUpdateWares()
        }
    }

    private fun onUpdateWares()
    {
        UpdateWaresPopupWindow.instance.show(mTextViewDebug, "正在获取库存数据") {}
        Task.AsyncHandler.post(UpdateWaresTask(onUiNotify))
    }

    private val onUiNotify = {
        UpdateWaresPopupWindow.instance.dismiss()
        onInitFragments()
        LocalBroadcastManager.getInstance(this@HomeActivity).sendAction(ACTION_ACQUIRE_WARES)
    }

    override fun onNewIntent(intent: Intent)
    {
        super.onNewIntent(intent)
        val ret = intent.getBooleanExtra(EXTRA_KEY_QUIT, false)
        if (ret) {
            App.ResetFlag = false
            finish()
        }
    }

    private fun initUi()
    {
        onInitFragments()
        mViewPager.addOnPageChangeListener(mPagerChangeListener)
        mButtonUp.setOnClickListener(::onButtonUp)
        mButtonDown.setOnClickListener(::onButtonDown)
        mTextViewDebug.setOnLongClickListener(::onDebug)
        mButtonUp.isEnabled = false
        mButtonUp.setBackgroundResource(R.drawable.shape_main_disable_button)
    }

    private fun onButtonUp(view: View)
    {
        val itemIndex = mViewPager.currentItem
        if (itemIndex <= 0) {
            return
        }
        mViewPager.currentItem = itemIndex - 1
    }

    private fun onButtonDown(view: View)
    {
        val itemIndex = mViewPager.currentItem
        if (itemIndex >= (mAdapter!!.count - 1)) {
            return
        }
        mViewPager.currentItem = itemIndex + 1
    }

    private val mPagerChangeListener = object: ViewPager.OnPageChangeListener
    {
        override fun onPageScrollStateChanged(state: Int)
        {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
        {
        }

        override fun onPageSelected(position: Int)
        {
            val itemIndex = mViewPager.currentItem

            mButtonUp.isEnabled = true
            mButtonDown.isEnabled = true
            mButtonUp.setBackgroundResource(R.drawable.selector_main_button)
            mButtonDown.setBackgroundResource(R.drawable.selector_main_button)

            if (itemIndex == 0) {
                mButtonUp.isEnabled = false
                mButtonUp.setBackgroundResource(R.drawable.shape_main_disable_button)
            }

            if (itemIndex == (mAdapter!!.count - 1)) {
                mButtonDown.isEnabled = false
                mButtonDown.setBackgroundResource(R.drawable.shape_main_disable_button)
            }
        }
    }

    private fun onDebug(view: View): Boolean
    {
        LoginPopupWindow.instance.show(mTextViewDebug) {
            App.ResetFlag = false
            val i = Intent(this@HomeActivity, DebugActivity::class.java)
            startActivity(i)
        }
        return true
    }

    override fun onStart()
    {
        super.onStart()
        App.ResetFlag = true
    }

    override fun onStop()
    {
        super.onStop()
        if (App.ResetFlag) {
            resetApp()
        }
    }

    override fun onDestroy()
    {
        mWaresUpdateReceiver.unregister()
        super.onDestroy()
        if (App.ResetFlag) {
            resetApp()
        }
    }

    private fun onInitFragments()
    {
        val currentIndex = if (mAdapter != null) { mViewPager.currentItem } else { 0 }
        mAdapter = ViewPagerAdapter(supportFragmentManager)
        mViewPager.adapter = mAdapter
        mViewPager.offscreenPageLimit = mAdapter!!.count
        mViewPager.currentItem = currentIndex
    }

    private val mWaresUpdateReceiver = object: BroadcastReceiver()
    {
        override fun onReceive(context: Context, intent: Intent)
        {
            when (intent.action)
            {
                App.ACTION_GET_WARES -> onInitFragments()
            }
        }
    }

    private class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager)
    {
        private val mFragments = ArrayList<Fragment>()

        init {
            var pages = (WaresManager.instance.getWaresNumber() + App.ITEM_COUNTER - 1) / App.ITEM_COUNTER
            if (pages == 0) {
                pages = 1
            }
            for (i in 0 until pages)
            {
                val fragment = MenuFragment()
                val bundle = Bundle()
                bundle.putInt(MenuFragment.EXTRA_INDEX_KEY, i)
                fragment.arguments = bundle
                mFragments.add(fragment)
            }
        }

        override fun getItem(position: Int) = mFragments[position]

        override fun getCount() = mFragments.size
    }
}

private class UpdateWaresTask(val onUiNotify: () -> Unit) : Runnable
{
    override fun run()
    {
        val ret = WaresManager.instance.updateWares()
        if (ret) {
            Task.UiHandler.post(onUiNotify)
            return
        }
        Task.AsyncHandler.postDelayed(this, 10000)
    }
}











