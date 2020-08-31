package jp.co.c_lis.mangaview.android

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import dev.keiji.mangaview.widget.Config
import dev.keiji.mangaview.widget.DoublePageLayoutManager
import dev.keiji.mangaview.widget.DoubleTapZoomHelper
import dev.keiji.mangaview.widget.EdgeNavigationHelper
import dev.keiji.mangaview.widget.HorizontalRtlLayoutManager
import dev.keiji.mangaview.widget.MangaView
import dev.keiji.mangaview.widget.OnDoubleTapListener
import dev.keiji.mangaview.widget.OnPageChangeListener
import dev.keiji.mangaview.widget.OnReadCompleteListener
import dev.keiji.mangaview.widget.PageLayout
import dev.keiji.mangaview.widget.SinglePageLayoutManager
import dev.keiji.mangaview.widget.VerticalLayoutManager
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

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val PAGE_WIDTH = 859
        private const val PAGE_HEIGHT = 1214

        private const val KEY_CURRENT_PAGE_INDEX = "state_key_page_index"
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var mangaView: MangaView? = null

    var currentToast: Toast? = null

    private val onPageChangeListener = object : OnPageChangeListener {
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

    private val onDoubleTapListener = object : OnDoubleTapListener {
        override fun onDoubleTap(mangaView: MangaView, x: Float, y: Float): Boolean {
            return true
        }
    }

    private val onReadCompleteListener = object : OnReadCompleteListener {
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
            it.adapter = AssetBitmapAdapter(
                assets, FILE_NAMES, coroutineScope,
                PAGE_WIDTH, PAGE_HEIGHT
            )
            it.addOnPageChangeListener(onPageChangeListener)
            it.addOnDoubleTapListener(onDoubleTapListener)
            it.addOnReadCompleteListener(onReadCompleteListener)

            DoubleTapZoomHelper().setup(it)
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
