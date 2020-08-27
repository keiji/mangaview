package dev.keiji.mangaview.widget

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

abstract class BitmapLayer : ContentLayer() {

    companion object {
        private val TAG = BitmapLayer::class.java.simpleName
    }

    @Volatile
    var bitmap: Bitmap? = null

    override val contentWidth: Float
        get() = bitmap?.width?.toFloat() ?: 0.0F
    override val contentHeight: Float
        get() = bitmap?.height?.toFloat() ?: 0.0F

    override val isContentPrepared: Boolean
        get() = bitmap != null

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

    override fun onRecycled() {
        super.onRecycled()

        bitmap?.recycle()
        bitmap = null
    }
}
