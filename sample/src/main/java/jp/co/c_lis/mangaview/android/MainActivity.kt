package jp.co.c_lis.mangaview.android

import android.content.res.Configuration
import android.graphics.PointF
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import dev.keiji.mangaview.Rectangle
import dev.keiji.mangaview.Region
import dev.keiji.mangaview.TiledSource
import dev.keiji.mangaview.Config
import dev.keiji.mangaview.widget.DoublePageLayoutManager
import dev.keiji.mangaview.DoubleTapZoomHelper
import dev.keiji.mangaview.EdgeNavigationHelper
import dev.keiji.mangaview.Mode
import dev.keiji.mangaview.widget.HorizontalRtlLayoutManager
import dev.keiji.mangaview.widget.MangaView
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.PageLayout
import dev.keiji.mangaview.layer.RegionLayer
import dev.keiji.mangaview.widget.SinglePageLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

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
    "comic_001bj_1.jpg",
)

private val TILED_SOURCE = TiledSource.build(2149F, 3035F, 256.0F, 256.0F)

// Tiled-images have been made by `script/tiled_bitmap_maker`.
private const val URL_PREFIX =
    "https://raw.githubusercontent.com/keiji/mangaview/master/sample/images"
private val TILED_IMAGE_URL_LIST = (0 until TILED_SOURCE.colCount * TILED_SOURCE.rowCount).map {
    "$URL_PREFIX/comic_001bj_2/comic_001bj_2-%04d.jpg".format(it)
}

private const val CATALOG_FILE_NAME = "mrdb.json"

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val PAGE_WIDTH = 859
        private const val PAGE_HEIGHT = 1214

        private const val DOUBLE_TAP_SCALE = 3.0F

        private const val KEY_CURRENT_PAGE_INDEX = "state_key_page_index"
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var mangaView: MangaView? = null

    private var doubleTapZoomHelper: DoubleTapZoomHelper? = null
    private var edgeNavigationHelper: EdgeNavigationHelper? = null

    var currentToast: Toast? = null

    private val onPageChangeListener = object : MangaView.OnPageChangeListener {
        override fun onPageLayoutSelected(mangaView: MangaView, pageLayout: PageLayout) {
            val page = pageLayout.keyPage ?: return

            currentToast?.cancel()

            currentToast = Toast.makeText(
                this@MainActivity,
                "Page Index: ${page.index}",
                Toast.LENGTH_SHORT
            ).also {
                it.show()
            }

        }
    }

    private val onDoubleTapListener = object : MangaView.OnDoubleTapListener {
        override fun onDoubleTap(mangaView: MangaView, viewX: Float, viewY: Float): Boolean {
            return false
        }
    }

    private val onReadCompleteListener = object : MangaView.OnReadCompleteListener {
        override fun onReadCompleted(mangaView: MangaView) {
            currentToast?.cancel()

            currentToast = Toast.makeText(
                this@MainActivity,
                "Read complete.",
                Toast.LENGTH_LONG
            ).also {
                it.show()
            }
        }

    }

    private val onSelectedRegionListener = object : RegionLayer.OnSelectedRegionListener {
        private val tmpSelectedRegionContent = Rectangle()
        private val tmpSelectedRegionGlobal = Rectangle()

        override fun onLongTapRegion(
            page: Page,
            layer: RegionLayer,
            region: Region,
            bounds: RectF
        ): Boolean {
            layer.projectionContentAndGlobal(
                bounds,
                tmpSelectedRegionContent,
                tmpSelectedRegionGlobal
            )
            mangaView?.focus(tmpSelectedRegionGlobal)
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        @Suppress("DEPRECATION")
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)

        val pageLayoutManager =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                SinglePageLayoutManager()
            } else {
                DoublePageLayoutManager(isSpread = true, startOneSide = true)
            }

        mangaView = findViewById<MangaView>(R.id.manga_view).also { mangaView ->
            mangaView.config = Config(
                mode = Mode.Normal,
                resetScaleOnPageChanged = true,
            )
            mangaView.layoutManager = HorizontalRtlLayoutManager()
            mangaView.pageLayoutManager = pageLayoutManager
            mangaView.adapter = SampleBitmapAdapter(
                assets, FILE_NAMES,
                CATALOG_FILE_NAME,
                TILED_SOURCE, TILED_IMAGE_URL_LIST,
                cacheDir,
                onSelectedRegionListener,
                coroutineScope,
                PAGE_WIDTH, PAGE_HEIGHT
            )
            mangaView.addOnPageChangeListener(onPageChangeListener)
            mangaView.addOnDoubleTapListener(onDoubleTapListener)
            mangaView.addOnReadCompleteListener(onReadCompleteListener)

            doubleTapZoomHelper = DoubleTapZoomHelper(maxScale = DOUBLE_TAP_SCALE).also {
                it.attachToMangaView(mangaView)
            }
            edgeNavigationHelper = EdgeNavigationHelper().also {
                it.attachToMangaView(mangaView)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        coroutineScope.cancel()

        mangaView?.also { mangaView ->
            doubleTapZoomHelper?.detachToMangaView(mangaView)
            edgeNavigationHelper?.detachToMangaView(mangaView)
        }
    }
}
