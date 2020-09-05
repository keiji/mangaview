package dev.keiji.mangaview.widget

import android.view.animation.DecelerateInterpolator
import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle

class Animator {
    companion object {
        private val TAG = Animator::class.java.simpleName

        private const val DEFAULT_SCALE_DURATION = 230L
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

    fun computeAnimation(viewContext: ViewContext): Boolean {
        val elapsed = System.currentTimeMillis() - startTimeInMills
        val input = elapsed.toFloat() / duration
        Log.d(
            TAG,
            "startTimeInMills ${startTimeInMills}, elapsed $elapsed, duration $duration, input $input"
        )
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

        Log.d(TAG, "viewContext.viewport", viewContext.viewport)
        return true
    }

}
