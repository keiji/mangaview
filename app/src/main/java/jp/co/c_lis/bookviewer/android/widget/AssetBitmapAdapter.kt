package jp.co.c_lis.bookviewer.android.widget

import android.content.res.AssetManager

data class AssetBitmap(
    val fileName: String,
    val width: Int,
    val height: Int
)

class AssetBitmapAdapter(
    private val assetManager: AssetManager,
    private val assetBitmaps: Array<AssetBitmap>
) : PageAdapter() {

    override val pageCount = assetBitmaps.size

    override fun getPage(index: Int): Page {
        val data = assetBitmaps[index]
        return Page(index, data.width, data.height).also {
            it.layers.add(AssetBitmapLayer(assetManager, data.fileName))
        }
    }
}
