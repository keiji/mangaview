package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Log
import jp.co.c_lis.bookviewer.android.Rectangle

data class ViewState(
    internal var viewWidth: Float = 0.0F,
    internal var viewHeight: Float = 0.0F,
    internal var scrollX: Float = 0.0F,
    internal var scrollY: Float = 0.0F,
    internal var currentScale: Float = 1.0F,
    internal val viewport: Rectangle = Rectangle(0.0F, 0.0F, viewWidth, viewHeight),
    internal val scrollableArea: Rectangle = Rectangle(0.0F, 0.0F, 1.0F, 1.0F)
) {

    companion object {
        private val TAG = ViewState::class.java.simpleName
    }

    var minScale = 1.0F
    var maxScale = 5.0F

    val width: Float
        get() = viewWidth / currentScale

    val height: Float
        get() = viewHeight / currentScale

    private fun validate(): Boolean {
        var result = true

        viewport.set(
            scrollX,
            scrollY,
            scrollX + width,
            scrollY + height
        )

        if (viewport.left < scrollableArea.left) {
            offset(scrollableArea.left - viewport.left, 0.0F)
            result = false
        } else if (viewport.right > scrollableArea.right) {
            offset(scrollableArea.right - viewport.right, 0.0F)
            result = false
        }
        if (viewport.top < scrollableArea.top) {
            offset(0.0F, scrollableArea.top - viewport.top)
            result = false
        } else if (viewport.bottom > scrollableArea.bottom) {
            offset(0.0F, scrollableArea.bottom - viewport.bottom)
            result = false
        }

        return result
    }

    fun onScroll(
        distanceX: Float,
        distanceY: Float
    ): Boolean = offset(
        distanceX / currentScale,
        distanceY / currentScale
    )

    fun offset(
        offsetX: Float,
        offsetY: Float
    ): Boolean {
        scrollX += offsetX
        scrollY += offsetY

        return validate()
    }

    fun offsetTo(x: Int, y: Int): Boolean {
        scrollX = x.toFloat()
        scrollY = y.toFloat()

        return validate()
    }

    var isScaling: Boolean = false

    fun onScaleBegin(): Boolean {
        Log.d(TAG, "onScaleBegin")

        isScaling = true
        return true
    }

    fun onScale(factor: Float, focusX: Float, focusY: Float): Boolean {
        val scale = currentScale * factor

        val result = setScale(
            scale,
            focusX,
            focusY
        )

        return result
    }

    fun setScale(
        scale: Float,
        focusX: Float,
        focusY: Float
    ): Boolean {
        var newScale = scale

        if (maxScale < newScale) {
            newScale = maxScale
        }
        if (minScale > newScale) {
            newScale = minScale
        }
        if (currentScale == newScale) {
            return false
        }

        val focusXRatio = focusX / viewWidth
        val focusYRatio = focusY / viewHeight

        val newViewportWidth = viewWidth / newScale
        val newViewportHeight = viewHeight / newScale

        val diffX = newViewportWidth - width
        val diffY = newViewportHeight - height

        scrollX -= diffX * focusXRatio
        scrollY -= diffY * focusYRatio

        currentScale = newScale

        return validate()
    }

    fun endScale() {
        isScaling = false
    }

    fun canScrollLeft(rectangle: Rectangle) = rectangle.left < viewport.left

    fun canScrollRight(rectangle: Rectangle) = rectangle.right > viewport.right

    fun canScrollTop(rectangle: Rectangle) = rectangle.top < viewport.top

    fun canScrollBottom(rectangle: Rectangle) = rectangle.bottom > viewport.bottom

}
