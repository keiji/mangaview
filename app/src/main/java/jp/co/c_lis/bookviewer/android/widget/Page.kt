package jp.co.c_lis.bookviewer.android.widget

import android.graphics.Canvas
import android.graphics.Paint
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.CoroutineScope

class Page(
    val number: Int
) {
    companion object {
        private val TAG = ContentLayer::class.java.simpleName
    }

    val position = Rectangle()

    val pageViewport = Rectangle(0.0F, 0.0F, 1.0F, 1.0F)
    val destOnView = Rectangle()

    internal val layers = ArrayList<ContentLayer>()

    fun draw(
        canvas: Canvas?,
        viewState: ViewState,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {
        Rectangle.and(viewState.viewport, position, pageViewport)
        pageViewport.relativeBy(position)

        Rectangle.and(destOnView.set(viewState.viewport), position, destOnView)
        destOnView.relativeBy(viewState.viewport)

        layers.forEach {
            if (!it.draw(canvas, viewState, this, paint, coroutineScope)) {
                return false
            }
        }
        return true
    }
}
