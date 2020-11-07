package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import dev.keiji.mangaview.Config
import dev.keiji.mangaview.widget.DoublePageLayoutManager
import dev.keiji.mangaview.DoubleTapZoomHelper
import dev.keiji.mangaview.EdgeNavigationHelper
import dev.keiji.mangaview.TiledSource
import dev.keiji.mangaview.layer.BitmapLayer
import dev.keiji.mangaview.layer.RegionLayer
import dev.keiji.mangaview.layer.TiledBitmapLayer
import dev.keiji.mangaview.widget.HorizontalRtlLayoutManager
import dev.keiji.mangaview.widget.MangaView
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.PageAdapter
import dev.keiji.mangaview.widget.PageLayout
import dev.keiji.mangaview.widget.SinglePageLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val FILE_NAMES = arrayOf(
    "comic_001bj_2.jpg",
    "comic_001bj_3.jpg",
    "comic_001bj_4.jpg",
    "comic_001bj_5.jpg",
    "comic_001bj_6.jpg",
    "comic_001bj_7.jpg",
    "comic_001bj_8.jpg",
    "comic_001bj_9.jpg",
    "comic_001bj_10.jpg",
    "comic_001bj_11.jpg",
    "comic_001bj_12.jpg",
)

private const val FILE_NAME_LAST_PAGE = "comic_001bj_1.jpg"

class WithViewPager2Activity : AppCompatActivity() {

    companion object {
        private val TAG = WithViewPager2Activity::class.java.simpleName

        private const val PAGE_WIDTH = 859
        private const val PAGE_HEIGHT = 1214

        private const val DOUBLE_TAP_SCALE = 3.0F
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var viewPager: ViewPager2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        @Suppress("DEPRECATION")
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_with_viewpager2)

        viewPager = findViewById<ViewPager2>(R.id.viewpager).also { viewpager ->
            viewpager.adapter = MyAdapter(layoutInflater, assets, coroutineScope)
            ViewCompat.setLayoutDirection(viewpager, ViewCompat.LAYOUT_DIRECTION_RTL)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        coroutineScope.cancel()
    }

    class MangaViewHolder(
        itemView: View,
        private val assetManager: AssetManager,
        private val coroutineScope: CoroutineScope
    ) : RecyclerView.ViewHolder(itemView) {
        private val mangaView = itemView.findViewById<MangaView>(R.id.manga_view)
        private val doubleTapZoomHelper = DoubleTapZoomHelper(maxScale = DOUBLE_TAP_SCALE)

        fun bind() {
            mangaView.config = Config(
                resetScaleOnPageChanged = true
            )
            mangaView.layoutManager = HorizontalRtlLayoutManager()
            mangaView.pageLayoutManager = SinglePageLayoutManager()
            mangaView.adapter =
                SimpleAdapter(assetManager, FILE_NAMES, coroutineScope, PAGE_WIDTH, PAGE_HEIGHT)
            doubleTapZoomHelper.attachToMangaView(mangaView)
        }

        fun unbind() {
            doubleTapZoomHelper.detachToMangaView(mangaView)
        }
    }

    class LastPageHolder(
        itemView: View,
        private val assetManager: AssetManager,
        private val coroutineScope: CoroutineScope
    ) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.image_view)

        fun bind() {
            coroutineScope.launch(Dispatchers.IO) {
                assetManager.open(FILE_NAME_LAST_PAGE).use {
                    val bitmap = BitmapFactory.decodeStream(it)
                    showBitmap(bitmap)
                }
            }
        }

        suspend fun showBitmap(bitmap: Bitmap) = withContext(Dispatchers.Main) {
            imageView.setImageBitmap(bitmap)
        }
    }

    class UnknownHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class MyAdapter(
        private val inflater: LayoutInflater,
        private val assetManager: AssetManager,
        private val coroutineScope: CoroutineScope
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_MANGA_VIEW = 0
            private const val VIEW_TYPE_LAST_PAGE = 1
            private const val VIEW_TYPE_UNKNOWN = -1
        }

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                0 -> VIEW_TYPE_MANGA_VIEW
                1 -> VIEW_TYPE_LAST_PAGE
                else -> VIEW_TYPE_UNKNOWN
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_MANGA_VIEW -> MangaViewHolder(
                    inflater.inflate(
                        R.layout.view_item_manga_view,
                        parent,
                        false
                    ),
                    assetManager,
                    coroutineScope
                )
                VIEW_TYPE_LAST_PAGE -> LastPageHolder(
                    inflater.inflate(
                        R.layout.view_item_last_page,
                        parent,
                        false
                    ),
                    assetManager,
                    coroutineScope
                )
                else -> UnknownHolder(inflater.inflate(R.layout.view_item_unknown, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is MangaViewHolder -> {
                    holder.bind()
                }
                is LastPageHolder -> {
                    holder.bind()
                }
            }
        }

        override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
            super.onViewRecycled(holder)

            when (holder) {
                is MangaViewHolder -> {
                    holder.unbind()
                }
            }
        }

        override fun getItemCount(): Int = 2

    }

    class SimpleAdapter(
        private val assetManager: AssetManager,
        private val fileNames: Array<String>,
        private val coroutineScope: CoroutineScope,
        private val pageWidth: Int,
        private val pageHeight: Int
    ) : PageAdapter() {

        override val pageCount = fileNames.size

        override fun getPageWidth(index: Int) = pageWidth

        override fun getPageHeight(index: Int) = pageHeight

        override fun onConstructPage(index: Int, page: Page) {
            if (index < 0) {
                return
            }
            if (index >= fileNames.size) {
                return
            }

            val fileName = fileNames[index]
            page.addLayer(BitmapLayer(AssetBitmapSource(assetManager, fileName, coroutineScope)))
        }
    }
}
