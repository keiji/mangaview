package jp.co.c_lis.bookviewer.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.co.c_lis.bookviewer.android.widget.AssetBitmapAdapter
import jp.co.c_lis.bookviewer.android.widget.BookView
import jp.co.c_lis.bookviewer.android.widget.HorizontalLayoutManager

private val FILE_NAMES = arrayOf(
    "sample1.png",
    "sample2.png",
    "sample3.png",
    "sample4.png",
    "sample5.png",
    "sample6.png"
)

class MainActivity : AppCompatActivity() {

    private var bookView: BookView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bookView = findViewById<BookView>(R.id.book_view).also {
            it.layoutManager = HorizontalLayoutManager(reversed = true)
            it.adapter = AssetBitmapAdapter(assets, FILE_NAMES)
        }
    }
}
