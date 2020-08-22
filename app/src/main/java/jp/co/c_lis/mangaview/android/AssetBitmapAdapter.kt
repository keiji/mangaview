package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import jp.co.c_lis.mangaview.widget.Page
import jp.co.c_lis.mangaview.widget.PageAdapter

class AssetBitmapAdapter(
    private val assetManager: AssetManager,
    private val fileNames: Array<String>,
    pageWidth: Int,
    pageHeight: Int
) : PageAdapter(pageWidth, pageHeight) {

    override val pageCount = fileNames.size

    override fun getPage(index: Int): Page {
        val fileName = fileNames[index]
        return Page(index, pageWidth, pageHeight).also {
            it.addLayer(AssetBitmapLayer(assetManager, fileName))
        }
    }
}
