package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

class DummyAdapter(pageWidth: Int, pageHeight: Int) : PageAdapter(pageWidth, pageHeight) {
    override val pageCount: Int
        get() = 4

    override fun getPage(index: Int): Page {
        return Page(index, pageWidth, pageHeight).also {
            it.addLayer(DummyLayer(pageWidth.toFloat(), pageHeight.toFloat()))
        }
    }

}

class DummyLayer(
    private val cWidth: Float,
    private val cHeight: Float
) : ContentLayer() {

    override val contentWidth: Float
        get() = cWidth
    override val contentHeight: Float
        get() = cHeight

    private var isContentLoaded = false

    override val isContentPrepared: Boolean
        get() = isContentLoaded

    override fun onDraw(
        canvas: Canvas?,
        srcRect: Rect,
        dstRect: RectF,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean = true

    override fun onContentPrepared(viewContext: ViewContext, page: Page): Boolean {
        isContentLoaded = true
        return true
    }
}
