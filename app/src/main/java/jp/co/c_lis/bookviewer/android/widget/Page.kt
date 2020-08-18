package jp.co.c_lis.bookviewer.android.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.CoroutineScope

class Page(
    val index: Int
) {
    companion object {
        private val TAG = Page::class.java.simpleName
    }

    val position = Rectangle()

    val contentSrc = Rectangle()
    val projection = Rectangle()

    internal val layers = ArrayList<ContentLayer>()

    fun draw(
        canvas: Canvas?,
        viewState: ViewState,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {
        contentSrc
            .set(viewState.viewport)
            .and(position)
            ?.relativeBy(position)

        projection
            .set(viewState.viewport)
            .and(position)
            ?.relativeBy(viewState.viewport)
        normalize(projection, viewState)

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
        val leftRatio = rectangle.left / viewState.scaledWidth
        val rightRatio = rectangle.right / viewState.scaledWidth
        val topRatio = rectangle.top / viewState.scaledHeight
        val bottomRatio = rectangle.bottom / viewState.scaledHeight

        rectangle.left = viewState.viewWidth * leftRatio
        rectangle.right = viewState.viewWidth * rightRatio
        rectangle.top = viewState.viewHeight * topRatio
        rectangle.bottom = viewState.viewHeight * bottomRatio
    }

    fun recycle() {
        Log.d(TAG, "recycle ${index}")

        layers.forEach {
            it.recycle()
        }
    }
}
