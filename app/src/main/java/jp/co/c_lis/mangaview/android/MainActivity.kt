package jp.co.c_lis.mangaview.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.keiji.mangaview.widget.HorizontalRtlLayoutManager
import dev.keiji.mangaview.widget.MangaView

private val FILE_NAMES = arrayOf(
    "sample1.png",
    "sample2.png",
    "sample3.png",
    "sample4.png",
    "sample5.png",
    "sample6.png",
)

class MainActivity : AppCompatActivity() {

    private var mangaView: MangaView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mangaView = findViewById<MangaView>(R.id.book_view).also {
            it.layoutManager = HorizontalRtlLayoutManager()
            it.adapter = AssetBitmapAdapter(assets, FILE_NAMES, 1150, 1700)
        }
    }
}
