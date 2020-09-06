package dev.keiji.mangaview.widget

import android.view.animation.DecelerateInterpolator
import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle
import kotlin.math.min

class Animator {
    companion object {
        private val TAG = Animator::class.java.simpleName

        private const val DEFAULT_SCALE_DURATION = 230L

        private fun correction(viewport: Rectangle, scrollableArea: Rectangle) {
            if (viewport.left < scrollableArea.left) {
                viewport.offset(scrollableArea.left - viewport.left, 0.0F)
            }
            if (viewport.right > scrollableArea.right) {
                viewport.offset(scrollableArea.right - viewport.right, 0.0F)
            }
            if (viewport.top < scrollableArea.top) {
                viewport.offset(0.0F, scrollableArea.top - viewport.top)
            }
            if (viewport.bottom > scrollableArea.bottom) {
                viewport.offset(0.0F, scrollableArea.bottom - viewport.bottom)
            }
        }
    }

    private val interpolator = DecelerateInterpolator()

    private var startTimeInMills = -1L
    private var duration = -1L

    private val fromViewport = Rectangle()
    private val toViewport = Rectangle()

    fun scale(
        viewContext: ViewContext,
        pageLayout: PageLayout?,
        scale: Float,
        focusOnViewX: Float,
        focusOnViewY: Float,
        duration: Long = DEFAULT_SCALE_DURATION
    ): Animator? {
        pageLayout ?: return null

        fromViewport.copyFrom(viewContext.viewport)

        val focusXRatio = focusOnViewX / viewContext.viewWidth
        val focusYRatio = focusOnViewY / viewContext.viewHeight

        val newViewportWidth = viewContext.viewWidth / scale
        val newViewportHeight = viewContext.viewHeight / scale

        val diffWidth = viewContext.viewportWidth - newViewportWidth
        val diffHeight = viewContext.viewportHeight - newViewportHeight

        val diffLeft = diffWidth * focusXRatio
        val diffRight = diffWidth - diffLeft
        val diffTop = diffHeight * focusYRatio
        val diffBottom = diffHeight - diffTop

        toViewport.copyFrom(viewContext.viewport).also {
            it.left += diffLeft
            it.top += diffTop
            it.right -= diffRight
            it.bottom -= diffBottom
        }

        val vc = viewContext.copy().also {
            it.scaleTo(scale, viewContext.currentX, viewContext.currentY)
        }
        val scrollableArea = pageLayout.getScaledScrollArea(vc)

        correction(toViewport, scrollableArea)

        startTimeInMills = System.currentTimeMillis()
        this.duration = duration

        return this
    }

    fun focus(
        viewContext: ViewContext,
        pageLayout: PageLayout?,
        focusRect: Rectangle,
        duration: Long = DEFAULT_SCALE_DURATION
    ): Animator? {
        pageLayout ?: return null
        fromViewport.copyFrom(viewContext.viewport)

        val scale = min(
            viewContext.viewWidth / focusRect.width,
            viewContext.viewHeight / focusRect.height
        )

        val scaledWidth = focusRect.width * scale
        val scaledHeight = focusRect.height * scale

        val scaledPaddingHorizontal = (viewContext.viewWidth - scaledWidth) / scale
        val scaledPaddingVertical = (viewContext.viewHeight - scaledHeight) / scale

        val paddingLeft = scaledPaddingHorizontal / 2
        val paddingTop = scaledPaddingVertical / 2
        val paddingRight = scaledPaddingHorizontal - paddingLeft
        val paddingBottom = scaledPaddingVertical - paddingTop

        val left = focusRect.left - paddingLeft
        val top = focusRect.top - paddingTop
        val right = focusRect.right + paddingRight
        val bottom = focusRect.bottom + paddingBottom

        toViewport.set(left, top, right, bottom)

        val vc = viewContext.copy().also {
            it.scaleTo(scale, viewContext.currentX, viewContext.currentY)
        }
        val scrollableArea = pageLayout.getScaledScrollArea(vc)

        correction(toViewport, scrollableArea)

        startTimeInMills = System.currentTimeMillis()
        this.duration = duration

        return this
    }

    fun computeAnimation(viewContext: ViewContext): Boolean {
        val elapsed = System.currentTimeMillis() - startTimeInMills
        val input = elapsed.toFloat() / duration

        if (input > 1.0F) {
            viewContext.setViewport(
                toViewport.left,
                toViewport.top,
                toViewport.right,
                toViewport.bottom
            )
            return false
        }

        val factor = interpolator.getInterpolation(input)

        val diffLeft = toViewport.left - fromViewport.left
        val diffTop = toViewport.top - fromViewport.top
        val diffRight = toViewport.right - fromViewport.right
        val diffBottom = toViewport.bottom - fromViewport.bottom

        viewContext.setViewport(
            fromViewport.left + diffLeft * factor,
            fromViewport.top + diffTop * factor,
            fromViewport.right + diffRight * factor,
            fromViewport.bottom + diffBottom * factor
        )

        return true
    }

}
