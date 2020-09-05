package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import dev.keiji.mangaview.BuildConfig
import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle

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
        onContentViewportChangeListener: (ContentLayer, RectF) -> Unit
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
                it.draw(
                    canvas,
                    viewContext,
                    this,
                    paint,
                    onContentViewportChangeListener
                )
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

    fun requestHandleOnTapEvent(
        globalX: Float,
        globalY: Float,
        onTapListenerList: List<OnTapListener>
    ): Boolean {
        localPointTmp.set(globalX, globalY, globalX, globalY)

        if (!globalRect.contains(localPointTmp)) {
            return false
        }

        val localPoint = localPointTmp
            .relativeBy(globalRect)

        var consumed = false
        onTapListenerList.forEach {
            if (it.onTap(this, localPoint.centerX, localPoint.centerY)) {
                consumed = true
                return@forEach
            }
        }
        return consumed
    }

    fun requestHandleOnDoubleTapEvent(
        globalX: Float,
        globalY: Float,
        onDoubleTapListenerList: List<OnDoubleTapListener>
    ): Boolean {
        localPointTmp.set(globalX, globalY, globalX, globalY)

        if (!globalRect.contains(localPointTmp)) {
            return false
        }

        val localPoint = localPointTmp
            .relativeBy(globalRect)

        var consumed = false

        onDoubleTapListenerList.forEach {
            if (it.onDoubleTap(this, localPoint.centerX, localPoint.centerY)) {
                consumed = true
                return@forEach
            }
        }

        return consumed
    }

    fun requestHandleOnLongTapEvent(
        globalX: Float,
        globalY: Float,
        onLongTapListener: OnLongTapListener? = null
    ): Boolean {
        localPointTmp.set(globalX, globalY, globalX, globalY)

        if (!globalRect.contains(localPointTmp)) {
            return false
        }

        val localPoint = localPointTmp
            .relativeBy(globalRect)

        return onLongTapListener?.onLongTap(this, localPoint.centerX, localPoint.centerY) ?: false
    }
}
