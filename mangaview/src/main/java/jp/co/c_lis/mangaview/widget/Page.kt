package jp.co.c_lis.mangaview.widget

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import jp.co.c_lis.mangaview.BuildConfig
import jp.co.c_lis.mangaview.Log
import jp.co.c_lis.mangaview.Rectangle
import kotlinx.coroutines.CoroutineScope

class Page(
    val index: Int,
    var width: Int,
    var height: Int
) {
    companion object {
        private val TAG = Page::class.java.simpleName
    }

    var horizontalAlign: PageHorizontalAlign = PageHorizontalAlign.Center
    var verticalAlign: PageVerticalAlign = PageVerticalAlign.Middle

    var baseScale: Float = 1.0F

    val scaledWidth
        get() = width * baseScale
    val scaledHeight
        get() = height * baseScale

    val position = Rectangle()

    val contentSrc = Rectangle()
    val projection = Rectangle()

    internal val layers = ArrayList<ContentLayer>()

    fun addLayer(layer: ContentLayer) {
        layers.add(layer)
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

        if (projection.area == 0.0F) {
            // do not draw
            return true
        }

        var result = true

        layers.forEach {
            if (!it.draw(canvas, viewState, this, paint, coroutineScope)) {
                result = false
            }
        }

        if (BuildConfig.DEBUG) {
            paint.apply {
                color = Color.BLUE
                style = Paint.Style.STROKE
            }
            canvas?.drawRect(projection.let {
                RectF(it.left, it.top, it.right, it.bottom)
            }, paint)
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
