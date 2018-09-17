package fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.App
import app.register
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.hontech.springclient.R
import com.wang.avi.AVLoadingIndicatorView

import data.WaresManager
import popup.DeliverPopupWindow
import activity.HomeActivity
import app.unregister
import popup.PayPopupWindow
import util.Http
import util.Task
import util.log
import util.xCreateRoundStateDrawable
import java.lang.Exception

class MenuFragment : Fragment()
{
    companion object
    {
        const val EXTRA_INDEX_KEY = "extra.index.key"
    }

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: RecyclerViewAdapter
    private var mIndex = 0

    override fun onAttach(context: Context?)
    {
        super.onAttach(context)
        val bundle = arguments!!
        mIndex = bundle.getInt(EXTRA_INDEX_KEY)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val view = inflater.inflate(R.layout.fragment_menu, null)
        initUi(view)
        mUpdateReceiver.register(HomeActivity.ACTION_ACQUIRE_WARES)
        return view
    }

    private val mUpdateReceiver = object: BroadcastReceiver()
    {
        override fun onReceive(context: Context?, intent: Intent)
        {
            when (intent.action)
            {
                HomeActivity.ACTION_ACQUIRE_WARES -> {
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroy()
    {
        mUpdateReceiver.unregister()
        super.onDestroy()
    }

    private fun initUi(view: View)
    {
        mRecyclerView = view.findViewById(R.id.id_menu_fragment_recycler_view)
        mAdapter = RecyclerViewAdapter(mIndex, mRecyclerView)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = GridLayoutManager(context!!, App.ITEM_H_COUNTER)
        mRecyclerView.addItemDecoration(RecyclerViewItemDecoration())
    }

    private class RecyclerViewItemDecoration : RecyclerView.ItemDecoration()
    {
        companion object
        {
            private val ITEM_WIDTH = App.context.resources.getDimension(R.dimen.x450).toInt()
            private val ITEM_HEIGHT = App.context.resources.getDimension(R.dimen.y550).toInt()
            private val H_SPACE = (App.WIDTH - (ITEM_WIDTH * App.ITEM_H_COUNTER)) / (App.ITEM_H_COUNTER + 1)
            private val V_SPACE = (App.HEIGHT - (ITEM_HEIGHT * App.ITEM_V_COUNTER) - App.context.resources.getDimension(R.dimen.y200).toInt()) / (App.ITEM_V_COUNTER + 1)
        }

        override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView?)
        {
            super.getItemOffsets(outRect, itemPosition, parent)
            val col = itemPosition % App.ITEM_H_COUNTER
            if (itemPosition < App.ITEM_H_COUNTER) {
                outRect.top = V_SPACE
            }
            outRect.bottom = V_SPACE
            outRect.left = H_SPACE - col * H_SPACE / 2
        }
    }

    private class RecyclerViewItem(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        companion object
        {
            private val WIDTH = App.context.resources.getDimension(R.dimen.x450).toInt()
            private val RADIUS = App.context.resources.getDimension(R.dimen.x50)
        }

        private val mLinearLayout = itemView.findViewById<LinearLayout>(R.id.id_item_linear_layout)
        private val mImageView = itemView.findViewById<ImageView>(R.id.id_item_image_view)
        private val mTextViewShadow = itemView.findViewById<TextView>(R.id.id_item_text_view_shadow)
        private val mTextViewName = itemView.findViewById<TextView>(R.id.id_item_text_view_name)
        private val mLoading = itemView.findViewById<AVLoadingIndicatorView>(R.id.id_item_loading)

        fun set(position: Int, parent: View)
        {
            val info = WaresManager.instance.get(position)
            mLoading.hide()
            mImageView.setTag(R.id.imageId, info.minImage)
        
            Glide.with(App.context)
                    .load(info.minImage)
                    .asBitmap()
                    .override(WIDTH, WIDTH)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(true)
                    .into(object: SimpleTarget<Bitmap>() {

                override fun onLoadStarted(placeholder: Drawable?)
                {
                    val url = mImageView.getTag(R.id.imageId)
                    if (url != null && (url as String) == info.minImage)
                    {
                        mImageView.visibility = View.GONE
                        mLoading.show()
                    }
                }

                override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?)
                {
                    val url = mImageView.getTag(R.id.imageId)
                    if (url != null && (url as String) == info.minImage)
                    {
                        mImageView.visibility = View.VISIBLE
                        mImageView.setImageResource(R.drawable.ic_network_error)
                        mLoading.hide()
                    }
                }

                override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>?)
                {
                    val url = mImageView.getTag(R.id.imageId)
                    if (url != null && (url as String) == info.minImage)
                    {
                        val drawable = xCreateRoundStateDrawable(resource, RADIUS)
                        mImageView.visibility = View.VISIBLE
                        mImageView.setImageDrawable(drawable)
                        mLoading.hide()
                    }
                }
            })
            mTextViewName.text = "${info.name}:¥${info.price}"

            if (info.sumNumber <= 0) {
                mTextViewShadow.visibility = View.VISIBLE
                mLinearLayout.setOnClickListener {  }
            } else {
                mTextViewShadow.visibility = View.GONE
                mLinearLayout.setOnClickListener {
                    WaresManager.instance.SelectIndex = position
                    PayPopupWindow.instance.show(parent) {
                        DeliverPopupWindow.instance.show(parent)
                    }
                }
            }
        }
    }

    private class RecyclerViewAdapter(val index: Int, val parent: View) : RecyclerView.Adapter<RecyclerViewItem>()
    {
        override fun onBindViewHolder(holder: RecyclerViewItem, position: Int)
        {
            val offset = index * App.ITEM_COUNTER + position
            holder.set(offset, parent)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewItem
        {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
            return RecyclerViewItem(view)
        }

        override fun getItemCount(): Int
        {
            val size = WaresManager.instance.getWaresNumber()
            val offset = index * App.ITEM_COUNTER
            val n = size - offset
            return if (n > App.ITEM_COUNTER) App.ITEM_COUNTER else n
        }
    }

}