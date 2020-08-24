package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.PageAdapter
import kotlinx.coroutines.CoroutineScope

class AssetBitmapAdapter(
    private val assetManager: AssetManager,
    private val fileNames: Array<String>,
    private val coroutineScope: CoroutineScope,
    pageWidth: Int,
    pageHeight: Int
) : PageAdapter(pageWidth, pageHeight) {

    override val pageCount = fileNames.size

    override fun getPage(index: Int): Page {
        val fileName = fileNames[index]
        return Page(index, pageWidth, pageHeight).also {
            it.addLayer(AssetBitmapLayer(assetManager, fileName, coroutineScope))
        }
    }
}
