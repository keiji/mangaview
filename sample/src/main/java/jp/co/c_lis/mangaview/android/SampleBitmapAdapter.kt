package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import dev.keiji.mangaview.Region
import dev.keiji.mangaview.TiledSource
import dev.keiji.mangaview.widget.BitmapLayer
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.PageAdapter
import dev.keiji.mangaview.widget.PathLayer
import dev.keiji.mangaview.widget.PathSource
import dev.keiji.mangaview.widget.TiledBitmapLayer
import kotlinx.coroutines.CoroutineScope
import java.io.File

class SampleBitmapAdapter(
    private val assetManager: AssetManager,
    private val fileNames: Array<String>,
    private val tiledSource: TiledSource,
    private val urlList: List<String>,
    private val tmpDir: File,
    private val regionList: ArrayList<Region>,
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
        page.addLayer(BitmapLayer(AssetBitmapSource(assetManager, fileName, coroutineScope)))

        if (index == 0) {
            val tiledImageSource = HttpServerTiledBitmapSource(
                tiledSource,
                urlList,
                tmpDir,
                coroutineScope
            )
            page.addLayer(TiledBitmapLayer(tiledImageSource))
        } else if (index == 4) {
            val pathSource = AssetPathSource(
                assetManager, fileName,
                regionList,
                coroutineScope
            )
            page.addLayer(PathLayer(pathSource))
        }

    }
}
