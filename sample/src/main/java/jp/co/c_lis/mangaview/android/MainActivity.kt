package jp.co.c_lis.mangaview.android

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import dev.keiji.mangaview.widget.DoublePageLayoutManager
import dev.keiji.mangaview.widget.HorizontalRtlLayoutManager
import dev.keiji.mangaview.widget.MangaView
import dev.keiji.mangaview.widget.OnDoubleTapListener
import dev.keiji.mangaview.widget.OnPageChangeListener
import dev.keiji.mangaview.widget.PageLayout
import dev.keiji.mangaview.widget.SinglePageLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

private val FILE_NAMES = arrayOf(
    "sample1.png",
    "sample2.png",
    "sample3.png",
    "sample4.png",
    "sample5.png",
    "sample6.png",
)

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val KEY_CURRENT_PAGE_INDEX = "state_key_page_index"
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var mangaView: MangaView? = null

    private val onPageChangeListener = object : OnPageChangeListener {
        override fun onPageLayoutSelected(mangaView: MangaView, pageLayout: PageLayout) {
            val page = pageLayout.keyPage ?: return
            Toast.makeText(
                this@MainActivity,
                "Page Index: ${page.index}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val onDoubleTapListener = object : OnDoubleTapListener {
        override fun onDoubleTap(mangaView: MangaView, x: Float, y: Float): Boolean {
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
                DoublePageLayoutManager(isSpread = true)
            }

        mangaView = findViewById<MangaView>(R.id.manga_view).also {
            it.layoutManager = HorizontalRtlLayoutManager()
            it.pageLayoutManager = pageLayoutManager
            it.adapter = AssetBitmapAdapter(
                assets, FILE_NAMES, coroutineScope,
                1150, 1700
            )
            it.onPageChangeListener = onPageChangeListener
            it.onDoubleTapListener = onDoubleTapListener
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
