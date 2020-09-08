package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import dev.keiji.mangaview.layer.ContentLayer
import dev.keiji.mangaview.source.ContentSource

class DummyAdapter(private val pageWidth: Int, private val pageHeight: Int) : PageAdapter() {
    override val pageCount: Int
        get() = 4

    override fun getPageWidth(index: Int) = pageWidth

    override fun getPageHeight(index: Int) = pageHeight

    override fun onConstructPage(index: Int, page: Page) {
        page.addLayer(DummyLayer(DummyContentSource(pageWidth.toFloat(), pageHeight.toFloat())))
    }
}

class DummyContentSource(
    private val cWidth: Float,
    private val cHeight: Float
) : ContentSource() {

    override val contentWidth: Float
        get() = cWidth
    override val contentHeight: Float
        get() = cHeight

    override fun getState(viewContext: ViewContext): State {
        return State.Prepared
    }

    override fun prepare(viewContext: ViewContext, onImageSourceLoaded: () -> Unit): Boolean {
        onImageSourceLoaded()
        return true
    }

    override fun recycle() {
    }

}

class DummyLayer(
    dummyImageSource: DummyContentSource
) : ContentLayer(dummyImageSource) {

    override fun onDraw(
        canvas: Canvas?,
        page: Page,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean = true
}
