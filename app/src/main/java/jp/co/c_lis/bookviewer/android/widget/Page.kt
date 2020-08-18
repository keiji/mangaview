package jp.co.c_lis.bookviewer.android.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import jp.co.c_lis.bookviewer.android.Log
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

    fun setAlignment(
        horizontal: PageHorizontalAlign = PageHorizontalAlign.Center,
        vertical: PageVerticalAlign = PageVerticalAlign.Middle
    ) {
        layers.forEach {
            it.setAlignment(horizontal, vertical)
        }
    }

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
        project(projection, viewState, projection)

        var result = true

        layers.forEach {
            if (!it.draw(canvas, viewState, this, paint, coroutineScope)) {
                result = false
            }
        }

        return result
    }

    private fun project(
        rectangle: Rectangle,
        viewState: ViewState,
        result: Rectangle
    ) {
        val leftRatio = rectangle.left / viewState.scaledWidth
        val rightRatio = rectangle.right / viewState.scaledWidth
        val topRatio = rectangle.top / viewState.scaledHeight
        val bottomRatio = rectangle.bottom / viewState.scaledHeight

        result.left = viewState.viewWidth * leftRatio
        result.right = viewState.viewWidth * rightRatio
        result.top = viewState.viewHeight * topRatio
        result.bottom = viewState.viewHeight * bottomRatio
    }

    fun recycle() {
        Log.d(TAG, "recycle ${index}")

        layers.forEach {
            it.recycle()
        }
    }
}
