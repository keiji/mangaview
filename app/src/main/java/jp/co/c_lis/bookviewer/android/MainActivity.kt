package jp.co.c_lis.bookviewer.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.co.c_lis.bookviewer.android.widget.*

private val DATA = arrayOf(
    AssetBitmap("sample1.png", 1150, 1700),
    AssetBitmap("sample2.png", 1150, 1700),
    AssetBitmap("sample3.png", 1150, 1700),
    AssetBitmap("sample4.png", 1150, 1700),
    AssetBitmap("sample5.png", 1150, 1700),
    AssetBitmap("sample6.png", 1150, 1700),
)

class MainActivity : AppCompatActivity() {

    private var bookView: BookView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bookView = findViewById<BookView>(R.id.book_view).also {
            it.layoutManager = HorizontalRtlLayoutManager()
            it.adapter = AssetBitmapAdapter(assets, DATA)
        }
    }
}
