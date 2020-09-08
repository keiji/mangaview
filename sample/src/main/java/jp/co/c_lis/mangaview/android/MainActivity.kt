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
import dev.keiji.mangaview.widget.DoubleTapZoomHelper
import dev.keiji.mangaview.widget.EdgeNavigationHelper
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

private val PAGE5_REGION_LIST = arrayListOf(

    // base image = "comic_001bj_6.jpg"
    // base image width = 859
    // base image height = 1214

    // 1
    Region(isNormalized = true).also { region ->
        region.addPoint(PointF(0.42142025F, 0.0F)) // 0.42142025F <- (362.0F / 859)
        region.addPoint(PointF(0.42142025F, 0.46952224F)) // 0.46952224F <- (570.0F / 1214)
        region.addPoint(PointF(0.89173457F, 0.46952224F)) // 0.89173457F <- (766.0F / 859)
        region.addPoint(PointF(0.89173457F, 0.0F))
    },

    // 2
    Region(isNormalized = true).also { region ->
        region.addPoint(PointF(0.0F, 0.0F))
        region.addPoint(PointF(0.42142025F, 0.0F)) // 0.42142025F <- (362.0F / 859)
        region.addPoint(PointF(0.42142025F, 0.28830313F)) // 0.28830313F <- (350.0F / 1214)
        region.addPoint(PointF(0.0F, 0.28830313F))
    },

    // 3
    Region(isNormalized = true).also { region ->
        region.addPoint(PointF(0.0F, 0.29159802F)) // 0.29159802F <- (354.0F / 1214)
        region.addPoint(PointF(0.42142025F, 0.29159802F)) // 0.42142025F <- (362.0F / 859)
        region.addPoint(PointF(0.42142025F, 0.47364085F)) // 0.47364085F <- (575.0F / 1214)
        region.addPoint(PointF(0.0F, 0.47364085F))
    },

    // 4
    Region(isNormalized = true).also { region ->
        region.addPoint(PointF(0.0F, 0.47775947F)) // 0.47775947F <- (580.0F / 1214)
        region.addPoint(PointF(0.89173457F, 0.47775947F)) // 0.89173457F <- (766.0F / 859)
        region.addPoint(PointF(0.89173457F, 1.0F))
        region.addPoint(PointF(0.0F, 1.0F))
    }
)

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
        override fun onDoubleTap(mangaView: MangaView, x: Float, y: Float): Boolean {
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

        override fun onDoubleTapRegion(
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
        this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)

        val pageLayoutManager =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                SinglePageLayoutManager()
            } else {
                DoublePageLayoutManager(isSpread = true, startOneSide = true)
            }

        mangaView = findViewById<MangaView>(R.id.manga_view).also {
            it.config = Config(
                resetScaleOnPageChanged = true
            )
            it.layoutManager = HorizontalRtlLayoutManager()
            it.pageLayoutManager = pageLayoutManager
            it.adapter = SampleBitmapAdapter(
                assets, FILE_NAMES,
                TILED_SOURCE, TILED_IMAGE_URL_LIST,
                cacheDir,
                PAGE5_REGION_LIST,
                onSelectedRegionListener,
                coroutineScope,
                PAGE_WIDTH, PAGE_HEIGHT
            )
            it.addOnPageChangeListener(onPageChangeListener)
            it.addOnDoubleTapListener(onDoubleTapListener)
            it.addOnReadCompleteListener(onReadCompleteListener)

            DoubleTapZoomHelper(maxScale = DOUBLE_TAP_SCALE).setup(it)
            EdgeNavigationHelper().setup(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        mangaView?.also {
            outState.putInt(KEY_CURRENT_PAGE_INDEX, it.currentPageIndex)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val currentPageIndex = savedInstanceState.getInt(KEY_CURRENT_PAGE_INDEX)
        mangaView?.currentPageIndex = currentPageIndex
    }

    override fun onDestroy() {
        super.onDestroy()

        coroutineScope.cancel()
    }
}
