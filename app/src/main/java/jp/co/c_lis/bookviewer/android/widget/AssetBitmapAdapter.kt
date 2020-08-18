package jp.co.c_lis.bookviewer.android.widget

import android.content.res.AssetManager

class AssetBitmapAdapter(
    private val assetManager: AssetManager,
    private val fileNames: Array<String>
) : PageAdapter() {

    override fun getPageCount() = fileNames.size

    override fun getPage(index: Int): Page {
        return Page(index).also {
            it.layers.add(AssetBitmapLayer(assetManager, fileNames[index]))
        }
    }
}
