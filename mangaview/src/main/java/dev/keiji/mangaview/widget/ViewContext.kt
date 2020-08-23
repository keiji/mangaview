package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Rectangle
import kotlin.math.max
import kotlin.math.min

data class ViewContext(
    internal var viewWidth: Float = 0.0F,
    internal var viewHeight: Float = 0.0F,
    internal var currentX: Float = 0.0F,
    internal var currentY: Float = 0.0F,
    internal var currentScale: Float = 1.0F,
    // Global Viewport
    internal val viewport: Rectangle = Rectangle(0.0F, 0.0F, viewWidth, viewHeight)
) {

    companion object {
        private val TAG = ViewContext::class.java.simpleName
    }

    var minScale = 1.0F
    var maxScale = 5.0F

    private val viewportWidth: Float
        get() = viewWidth / currentScale

    private val viewportHeight: Float
        get() = viewHeight / currentScale

    fun setViewSize(w: Int, h: Int) {
        viewWidth = w.toFloat()
        viewHeight = h.toFloat()

        viewport.left = 0.0F
        viewport.top = 0.0F
        viewport.right = viewWidth
        viewport.bottom = viewHeight
    }

    private fun applyViewport() {
        viewport.set(
            currentX,
            currentY,
            currentX + viewportWidth,
            currentY + viewportHeight
        )
    }

    fun scroll(
        distanceX: Float,
        distanceY: Float
    ) = offset(distanceX, distanceY)

    fun offset(
        offsetX: Float,
        offsetY: Float
    ) = offsetTo(currentX + offsetX, currentY + offsetY)

    fun offsetTo(x: Float, y: Float) {
        currentX = x
        currentY = y

        applyViewport()
    }

    fun scale(factor: Float, focusX: Float, focusY: Float) {
        val scale = currentScale * factor

        scaleTo(
            scale,
            focusX,
            focusY
        )
    }

    fun scaleTo(
        scale: Float,
        focusX: Float,
        focusY: Float
    ) {
        val newScale = max(min(scale, maxScale), minScale)
        if (currentScale == newScale) {
            return
        }

        val focusXRatio = focusX / viewWidth
        val focusYRatio = focusY / viewHeight

        val newViewportWidth = viewWidth / newScale
        val newViewportHeight = viewHeight / newScale

        val diffX = viewportWidth - newViewportWidth
        val diffY = viewportHeight - newViewportHeight

        offset(diffX * focusXRatio, diffY * focusYRatio)

        currentScale = newScale

        applyViewport()
    }

    fun canScrollLeft(rectangle: Rectangle, delta: Float = 0.0F): Boolean {
        return (rectangle.left - delta) < viewport.left
    }

    fun canScrollRight(rectangle: Rectangle, delta: Float = 0.0F): Boolean {
        return (rectangle.right + delta) > viewport.right
    }

    fun canScrollTop(rectangle: Rectangle, delta: Float = 0.0F): Boolean {
        return (rectangle.top - delta) < viewport.top
    }

    fun canScrollBottom(rectangle: Rectangle, delta: Float = 0.0F): Boolean {
        return (rectangle.bottom + delta) > viewport.bottom
    }
}
