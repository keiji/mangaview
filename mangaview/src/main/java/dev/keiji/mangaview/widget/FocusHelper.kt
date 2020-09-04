package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Rectangle
import kotlin.math.min

class FocusHelper() {

    companion object {
        private val TAG = FocusHelper::class.java.simpleName

        private const val DEFAULT_FOCUS_DURATION = 200L
    }

    private var focusDuration = DEFAULT_FOCUS_DURATION

    fun init(duration: Long): FocusHelper {
        focusDuration = duration

        return this
    }

    fun focus(
        viewContext: ViewContext,
        currentPageLayout: PageLayout,
        focusRect: Rectangle
    ): Animation {
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

        var left = focusRect.left - paddingLeft
        var top = focusRect.top - paddingTop
        var right = focusRect.right + paddingRight
        var bottom = focusRect.bottom + paddingBottom

        val scaledViewContext = viewContext.copy().also {
            it.scaleTo(
                scale,
                viewContext.viewWidth / 2,
                viewContext.viewHeight / 2
            )
        }
        val scrollableArea = currentPageLayout
            .getScaledScrollArea(scaledViewContext)

        if (scrollableArea.top > top) {
            val diff = scrollableArea.top - top
            top += diff
            bottom += diff
        }
        if (scrollableArea.left > left) {
            val diff = scrollableArea.left - left
            left += diff
            right += diff
        }
        if (scrollableArea.bottom < bottom) {
            val diff = bottom - scrollableArea.bottom
            top -= diff
            bottom -= diff
        }
        if (scrollableArea.right < right) {
            val diff = right - scrollableArea.right
            left -= diff
            right -= diff
        }

        return Animation(
            translate = Animation.Translate(
                viewContext.currentX, viewContext.currentY,
                left, top
            ),
            scale = Animation.Scale(
                viewContext.currentScale,
                scale,
                null,
                null
            ),
            durationMillis = focusDuration,
            priority = -1
        )
    }
}
