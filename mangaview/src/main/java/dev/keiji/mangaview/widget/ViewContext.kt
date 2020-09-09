package dev.keiji.mangaview.widget

import androidx.annotation.VisibleForTesting
import dev.keiji.mangaview.Rectangle
import kotlin.math.max
import kotlin.math.min

data class ViewContext(
    internal var viewWidth: Float = 0.0F,
    internal var viewHeight: Float = 0.0F,
    internal var currentX: Float = 0.0F,
    internal var currentY: Float = 0.0F,
    internal var currentScale: Float = 1.0F,
    internal var minScale: Float = 1.0F,
    internal var maxScale: Float = 5.0F,

    private var scrollPolicyHorizontal: Int = SCROLL_POLICY_UNLIMITED,
    private var scrollPolicyVertical: Int = SCROLL_POLICY_UNLIMITED
) {

    companion object {
        private val TAG = ViewContext::class.java.simpleName

        const val SCROLL_POLICY_UNLIMITED = 0
        const val SCROLL_POLICY_STRICT_SCROLL_AREA = 1
    }

    // Global Viewport
    internal val viewport: Rectangle = Rectangle(0.0F, 0.0F, viewWidth, viewHeight)

    @VisibleForTesting
    val viewportWidth: Float
        get() = viewWidth / currentScale

    @VisibleForTesting
    val viewportHeight: Float
        get() = viewHeight / currentScale

    internal fun setViewSize(w: Int, h: Int) {
        viewWidth = w.toFloat()
        viewHeight = h.toFloat()

        viewport.left = 0.0F
        viewport.top = 0.0F
        viewport.right = viewWidth
        viewport.bottom = viewHeight
    }

    internal fun applyViewport(scrollableArea: Rectangle? = null) {
        viewport.set(
            currentX,
            currentY,
            currentX + viewportWidth,
            currentY + viewportHeight
        )

        scrollableArea?.also {
            if (scrollPolicyHorizontal == SCROLL_POLICY_STRICT_SCROLL_AREA) {
                if (viewport.left < it.left) {
                    offset(it.left - viewport.left, 0.0F)
                } else if (viewport.right > it.right) {
                    offset(it.right - viewport.right, 0.0F)
                }
            }
            if (scrollPolicyVertical == SCROLL_POLICY_STRICT_SCROLL_AREA) {
                if (viewport.top < it.top) {
                    offset(0.0F, it.top - viewport.top)
                } else if (viewport.bottom > it.bottom) {
                    offset(0.0F, it.bottom - viewport.bottom)
                }
            }
        }
    }

    internal fun scroll(
        distanceX: Float,
        distanceY: Float,
        scrollArea: Rectangle? = null,
        applyImmediately: Boolean = true
    ) = offset(distanceX, distanceY, scrollArea, applyImmediately)

    internal fun offset(
        offsetX: Float,
        offsetY: Float,
        scrollArea: Rectangle? = null,
        applyImmediately: Boolean = true
    ) = offsetTo(currentX + offsetX, currentY + offsetY, scrollArea, applyImmediately)

    internal fun offsetTo(
        x: Float,
        y: Float,
        scrollArea: Rectangle? = null,
        applyImmediately: Boolean = true
    ) {
        currentX = x
        currentY = y

        if (applyImmediately) {
            applyViewport(scrollArea)
        }
    }

    internal fun scale(
        factor: Float,
        focusX: Float,
        focusY: Float,
        scrollArea: Rectangle?,
        applyImmediately: Boolean = true
    ) {
        val scale = currentScale * factor

        scaleTo(
            scale,
            focusX,
            focusY,
            scrollArea,
            applyImmediately
        )
    }

    internal fun scaleTo(
        scale: Float,
        focusViewX: Float = currentX,
        focusViewY: Float = currentY,
        scrollArea: Rectangle? = null,
        applyImmediately: Boolean = true
    ) {
        val newScale = max(min(scale, maxScale), minScale)
        if (currentScale == newScale) {
            return
        }

        val focusXRatio = focusViewX / viewWidth
        val focusYRatio = focusViewY / viewHeight

        val newViewportWidth = viewWidth / newScale
        val newViewportHeight = viewHeight / newScale

        val diffX = viewportWidth - newViewportWidth
        val diffY = viewportHeight - newViewportHeight

        currentScale = newScale

        offset(diffX * focusXRatio, diffY * focusYRatio, scrollArea, applyImmediately)
    }

    internal fun canScrollLeft(rectangle: Rectangle, delta: Float = 0.0F): Boolean {
        return (rectangle.left - delta) < viewport.left
    }

    internal fun canScrollRight(rectangle: Rectangle, delta: Float = 0.0F): Boolean {
        return (rectangle.right + delta) > viewport.right
    }

    internal fun canScrollTop(rectangle: Rectangle, delta: Float = 0.0F): Boolean {
        return (rectangle.top - delta) < viewport.top
    }

    internal fun canScrollBottom(rectangle: Rectangle, delta: Float = 0.0F): Boolean {
        return (rectangle.bottom + delta) > viewport.bottom
    }

    internal fun projectionGlobalPoint(viewX: Float, viewY: Float, result: Rectangle): Rectangle {
        val horizontalRatio = viewX / viewWidth
        val verticalRatio = viewY / viewHeight

        val globalX = viewport.left + viewport.width * horizontalRatio
        val globalY = viewport.top + viewport.height * verticalRatio

        return result
            .set(globalX, globalY, globalX, globalY)
    }

    internal fun projectionViewPoint(globalX: Float, globalY: Float, result: Rectangle): Rectangle {
        val horizontalRatio = (globalX - viewport.left) / viewportWidth
        val verticalRatio = (globalY - viewport.top) / viewportHeight

        val viewX = viewWidth * horizontalRatio
        val viewY = viewHeight * verticalRatio

        return result
            .set(viewX, viewY, viewX, viewY)
    }

    internal fun setScrollableAxis(horizontal: Int, vertical: Int) {
        scrollPolicyHorizontal = horizontal
        scrollPolicyVertical = vertical
    }

    internal fun applyViewport(left: Float, top: Float, right: Float, bottom: Float) {
        viewport.set(left, top, right, bottom)
        currentScale = viewWidth / viewport.width
        currentX = viewport.left
        currentY = viewport.top
    }
}
