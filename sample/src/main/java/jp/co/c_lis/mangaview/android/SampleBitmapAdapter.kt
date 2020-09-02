package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.PageAdapter
import dev.keiji.mangaview.widget.TiledSource
import kotlinx.coroutines.CoroutineScope

class SampleBitmapAdapter(
    private val assetManager: AssetManager,
    private val fileNames: Array<String>,
    private val tiledSource: TiledSource,
    private val tiledFileDirs: Array<String>,
    private val coroutineScope: CoroutineScope,
    private val pageWidth: Int,
    private val pageHeight: Int
) : PageAdapter() {

    override val pageCount = fileNames.size

    override fun getPageWidth(index: Int) = pageWidth

    override fun getPageHeight(index: Int) = pageHeight

    override fun onConstructPage(index: Int, page: Page) {
        if (index < 0) {
            return
        }
        if (index >= fileNames.size) {
            return
        }

        val fileName = fileNames[index]
        page.addLayer(AssetBitmapLayer(assetManager, fileName, coroutineScope))

        if (index == 0) {
            page.addLayer(
                AssetTiledBitmapLayer(
                    tiledSource,
                    assetManager, tiledFileDirs[index], coroutineScope
                )
            )
        }

    }
}
