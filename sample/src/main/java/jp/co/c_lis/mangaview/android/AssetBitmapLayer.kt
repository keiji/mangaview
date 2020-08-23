package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import dev.keiji.mangaview.widget.ContentLayer
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.ViewContext

class AssetBitmapLayer(
    private val assetManager: AssetManager,
    private val fileName: String,
) : ContentLayer() {

    companion object {
        private val TAG = AssetBitmapLayer::class.java.simpleName
    }

    @Volatile
    private var bitmap: Bitmap? = null

    override val contentWidth: Float
        get() = bitmap?.width?.toFloat() ?: 0.0F
    override val contentHeight: Float
        get() = bitmap?.height?.toFloat() ?: 0.0F

    override val isPrepared: Boolean
        get() = bitmap != null

    override fun prepareContent(viewContext: ViewContext, page: Page) {
        bitmap = assetManager.open(fileName).use {
            BitmapFactory.decodeStream(it)
        }
    }

    override fun onDraw(
        canvas: Canvas?,
        srcRect: Rect,
        dstRect: RectF,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean {
        val bitmapSnapshot = bitmap ?: return false

        canvas?.drawBitmap(
            bitmapSnapshot,
            srcRect,
            dstRect,
            paint
        )

        return true
    }

    override fun recycle() {
        super.recycle()

        bitmap?.recycle()
        bitmap = null
    }
}
