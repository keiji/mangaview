package jp.co.c_lis.mangaview.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.co.c_lis.mangaview.widget.AssetBitmap
import jp.co.c_lis.mangaview.widget.AssetBitmapAdapter
import jp.co.c_lis.mangaview.widget.BookView
import jp.co.c_lis.mangaview.widget.VerticalLayoutManager

private val DATA = arrayOf(
    AssetBitmap("sample1.png"),
    AssetBitmap("sample2.png"),
    AssetBitmap("sample3.png"),
    AssetBitmap("sample4.png"),
    AssetBitmap("sample5.png"),
    AssetBitmap("sample6.png"),
)

class MainActivity : AppCompatActivity() {

    private var bookView: BookView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bookView = findViewById<BookView>(R.id.book_view).also {
            it.layoutManager = VerticalLayoutManager()
            it.adapter = AssetBitmapAdapter(assets, DATA, 1150, 1700)
        }
    }
}
