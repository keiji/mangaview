package jp.co.c_lis.mangaview.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import dev.keiji.mangaview.widget.HorizontalRtlLayoutManager
import dev.keiji.mangaview.widget.MangaView
import dev.keiji.mangaview.widget.OnDoubleTapListener
import dev.keiji.mangaview.widget.OnPageChangeListener
import dev.keiji.mangaview.widget.PageLayout
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
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var mangaView: MangaView? = null

    private val onPageChangeListener = object : OnPageChangeListener {
        override fun onPageLayoutSelected(mangaView: MangaView, pageLayout: PageLayout) {
            val page = pageLayout.primaryPage ?: return
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

        mangaView = findViewById<MangaView>(R.id.manga_view).also {
            it.layoutManager = HorizontalRtlLayoutManager()
            it.adapter = AssetBitmapAdapter(
                assets, FILE_NAMES, coroutineScope,
                1150, 1700
            )
            it.onPageChangeListener = onPageChangeListener
            it.onDoubleTapListener = onDoubleTapListener
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        coroutineScope.cancel()
    }
}
