package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import dev.keiji.mangaview.Region
import dev.keiji.mangaview.TiledSource
import dev.keiji.mangaview.layer.BitmapLayer
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.PageAdapter
import dev.keiji.mangaview.layer.RegionLayer
import dev.keiji.mangaview.layer.TiledBitmapLayer
import kotlinx.coroutines.CoroutineScope
import java.io.File

class SampleBitmapAdapter(
    private val assetManager: AssetManager,
    private val fileNames: Array<String>,
    private val tiledSource: TiledSource,
    private val urlList: List<String>,
    private val tmpDir: File,
    private val onSelectedRegionListener: RegionLayer.OnSelectedRegionListener,
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

        val pathSource = CrdbRegionSource(
            assetManager, "crdb.json", fileName,
            tmpDir,
            coroutineScope
        )
        val pathLayer = RegionLayer(pathSource).also {
            it.addOnSelectRegionListener(onSelectedRegionListener)
        }
        page.addLayer(pathLayer)

        if (index == 0) {
            val tiledImageSource = NetworkTiledBitmapSource(
                tiledSource,
                urlList,
                tmpDir,
                coroutineScope
            )
            page.addLayer(TiledBitmapLayer(tiledImageSource))
        }
    }
}
