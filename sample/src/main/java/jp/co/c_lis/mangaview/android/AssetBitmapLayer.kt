package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import dev.keiji.mangaview.widget.BitmapLayer
import dev.keiji.mangaview.widget.ContentLayer
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.ViewContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AssetBitmapLayer(
    private val assetManager: AssetManager,
    private val fileName: String,
    private val coroutineScope: CoroutineScope,
) : BitmapLayer() {

    companion object {
        private val TAG = AssetBitmapLayer::class.java.simpleName
    }

    override fun onContentPrepared(viewContext: ViewContext, page: Page): Boolean {
        coroutineScope.launch(Dispatchers.IO) {
            bitmap = assetManager.open(fileName).use {
                BitmapFactory.decodeStream(it)
            }
        }

        return false
    }
}
