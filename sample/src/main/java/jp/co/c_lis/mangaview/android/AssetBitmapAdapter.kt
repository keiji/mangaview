package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.PageAdapter
import kotlinx.coroutines.CoroutineScope

class AssetBitmapAdapter(
    private val assetManager: AssetManager,
    private val fileNames: Array<String>,
    private val coroutineScope: CoroutineScope,
    private val pageWidth: Int,
    private val pageHeight: Int
) : PageAdapter() {

    override val pageCount = fileNames.size + 1 // page for detecting read complete

    override fun getPageWidth(index: Int) = pageWidth

    override fun getPageHeight(index: Int) = pageHeight

    override fun onConstructPage(index: Int, page: Page) {
        if (index >= fileNames.size) {
            return
        }

        val fileName = fileNames[index]
        page.addLayer(AssetBitmapLayer(assetManager, fileName, coroutineScope))

    }
}
