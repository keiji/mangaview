package jp.co.c_lis.bookviewer.android.widget

import android.content.res.AssetManager

data class AssetBitmap(
    val fileName: String
)

class AssetBitmapAdapter(
    private val assetManager: AssetManager,
    private val assetBitmaps: Array<AssetBitmap>,
    pageWidth: Int,
    pageHeight: Int
) : PageAdapter(pageWidth, pageHeight) {

    override val pageCount = assetBitmaps.size

    override fun getPage(index: Int): Page {
        val data = assetBitmaps[index]
        return Page(index, pageWidth, pageHeight).also {
            it.layers.add(AssetBitmapLayer(assetManager, data.fileName))
        }
    }
}
