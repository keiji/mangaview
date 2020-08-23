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

    val globalRect = Rectangle()

    val contentSrc = Rectangle()
    val displayProjection = Rectangle()

    internal val layers = ArrayList<ContentLayer>()

    fun addLayer(layer: ContentLayer) {
        layers.add(layer.also {
            it.page = this
        })
    }

    fun draw(
        canvas: Canvas?,
        viewContext: ViewContext,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {
        contentSrc
            .copyFrom(viewContext.viewport)
            .and(globalRect)
            ?.relativeBy(globalRect)

        displayProjection
            .copyFrom(viewContext.viewport)
            .and(globalRect)
            ?.relativeBy(viewContext.viewport)
        project(displayProjection, viewContext, displayProjection)

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
            canvas?.drawRect(displayProjection.let {
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

    private val localPointTmp = Rectangle()

    fun requestHandleEvent(
        globalX: Float,
        globalY: Float,
        onTapListener: OnTapListener
    ): Boolean {
        localPointTmp.set(globalX, globalY, globalX, globalY)

        if (!globalRect.contains(localPointTmp)) {
            return false
        }

        val localPoint = localPointTmp
            .relativeBy(globalRect)

        return onTapListener.onTap(this, localPoint.centerX, localPoint.centerY)
    }

    fun requestHandleEvent(
        globalX: Float,
        globalY: Float,
        onDoubleTapListener: OnDoubleTapListener
    ): Boolean {
        localPointTmp.set(globalX, globalY, globalX, globalY)

        if (!globalRect.contains(localPointTmp)) {
            return false
        }

        val localPoint = localPointTmp
            .relativeBy(globalRect)

        return onDoubleTapListener.onDoubleTap(this, localPoint.centerX, localPoint.centerY)
    }
}
