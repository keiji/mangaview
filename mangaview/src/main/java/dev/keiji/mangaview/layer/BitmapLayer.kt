package dev.keiji.mangaview.layer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.VisibleForTesting
import dev.keiji.mangaview.source.BitmapSource
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.ViewContext

class BitmapLayer(
    private val bitmapSource: BitmapSource
) : ContentLayer(bitmapSource) {

    companion object {
        private val TAG = BitmapLayer::class.java.simpleName
    }

    @VisibleForTesting
    val srcRect = Rect()

    @VisibleForTesting
    val dstRect = RectF()

    override fun onDraw(
        canvas: Canvas?,
        page: Page,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean {
        val bitmapSnapshot = bitmapSource.bitmap ?: return false

        contentSrc.copyTo(srcRect)
        page.displayProjection
            .copyTo(dstRect)

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
