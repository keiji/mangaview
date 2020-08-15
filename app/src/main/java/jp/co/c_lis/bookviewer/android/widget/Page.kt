package jp.co.c_lis.bookviewer.android.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.CoroutineScope

class Page(
    val number: Int
) {
    companion object {
        private val TAG = Page::class.java.simpleName
    }

    val position = Rectangle()

    val pageViewport = Rectangle()
    val contentSrc = Rectangle()
    val destOnView = Rectangle()

    internal val layers = ArrayList<ContentLayer>()

    fun draw(
        canvas: Canvas?,
        viewState: ViewState,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {
        pageViewport
            .set(viewState.viewport)
            .and(position)
            ?.relativeBy(position)

        contentSrc
            .set(viewState.viewport)
            .and(position)
            ?.relativeBy(position)

        destOnView
            .set(viewState.viewport)
            .and(position)
            ?.relativeBy(viewState.viewport)
        normalize(destOnView, viewState)

        var result = true

        layers.forEach {
            if (!it.draw(canvas, viewState, this, paint, coroutineScope)) {
                result = false
            }
        }

        return result
    }

    private fun normalize(
        rectangle: Rectangle,
        viewState: ViewState
    ) {
        val leftRatio = rectangle.left / viewState.width
        val rightRatio = rectangle.right / viewState.width
        val topRatio = rectangle.top / viewState.height
        val bottomRatio = rectangle.bottom / viewState.height

        rectangle.left = viewState.viewWidth * leftRatio
        rectangle.right = viewState.viewWidth * rightRatio
        rectangle.top = viewState.viewHeight * topRatio
        rectangle.bottom = viewState.viewHeight * bottomRatio
    }

    fun recycle() {
        Log.d(TAG, "recycle ${number}")

        layers.forEach {
            it.recycle()
        }
    }
}
