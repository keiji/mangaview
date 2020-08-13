package jp.co.c_lis.bookviewer.android.widget

import android.content.res.AssetManager

class AssetBitmapAdapter(
    private val assetManager: AssetManager,
    private val fileNames: Array<String>
) : PageAdapter() {

    override fun getPageCount() = fileNames.size

    override fun getPage(number: Int): Page {
        return Page(number).also {
            it.layers.add(AssetBitmapLayer(assetManager, fileNames[number]))
        }
    }
}
