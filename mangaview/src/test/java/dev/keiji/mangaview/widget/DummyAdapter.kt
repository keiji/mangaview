package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

class DummyAdapter(private val pageWidth: Int, private val pageHeight: Int) : PageAdapter() {
    override val pageCount: Int
        get() = 4

    override fun getPageWidth(index: Int) = pageWidth

    override fun getPageHeight(index: Int) = pageHeight

    override fun onConstructPage(index: Int, page: Page) {
        page.addLayer(DummyLayer(DummyImageSource(pageWidth.toFloat(), pageHeight.toFloat())))
    }
}

class DummyImageSource(
    private val cWidth: Float,
    private val cHeight: Float
) : ImageSource() {

    override val contentWidth: Float
        get() = cWidth
    override val contentHeight: Float
        get() = cHeight

    override fun getState(viewContext: ViewContext): State {
        return State.Loaded
    }

    override fun load(viewContext: ViewContext, onImageSourceLoaded: () -> Unit): Boolean {
        return true
    }

    override fun recycle() {
    }

}

class DummyLayer(
    dummyImageSource: DummyImageSource
) : ContentLayer(dummyImageSource) {

    override fun onDraw(
        canvas: Canvas?,
        srcRect: Rect,
        dstRect: RectF,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean = true
}
