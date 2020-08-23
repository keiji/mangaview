package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import dev.keiji.mangaview.BuildConfig
import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle
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
        viewContext: ViewContext,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {
        contentSrc
            .copyFrom(viewContext.viewport)
            .and(position)
            ?.relativeBy(position)

        projection
            .copyFrom(viewContext.viewport)
            .and(position)
            ?.relativeBy(viewContext.viewport)
        project(projection, viewContext, projection)

        val result = layers
            .map {
                it.draw(canvas, viewContext, this, paint, coroutineScope)
            }
            .none { !it }

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
        viewContext: ViewContext,
        result: Rectangle
    ) {
        val leftRatio = rectangle.left / viewContext.viewport.width
        val rightRatio = rectangle.right / viewContext.viewport.width
        val topRatio = rectangle.top / viewContext.viewport.height
        val bottomRatio = rectangle.bottom / viewContext.viewport.height

        result.left = viewContext.viewWidth * leftRatio
        result.right = viewContext.viewWidth * rightRatio
        result.top = viewContext.viewHeight * topRatio
        result.bottom = viewContext.viewHeight * bottomRatio
    }

    fun recycle() {
        Log.d(TAG, "recycle ${index}")

        layers.forEach {
            it.recycle()
        }
    }
}
