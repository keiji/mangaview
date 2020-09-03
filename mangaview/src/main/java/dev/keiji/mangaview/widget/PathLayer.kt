package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

class BitmapLayer(
    private val bitmapSource: BitmapSource
) : ContentLayer(bitmapSource) {

    companion object {
        private val TAG = BitmapLayer::class.java.simpleName
    }

    override fun onDraw(
        canvas: Canvas?,
        srcRect: Rect,
        dstRect: RectF,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean {
        val bitmapSnapshot = bitmapSource.bitmap ?: return false

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

        bitmapSource.recycle()
    }
}
