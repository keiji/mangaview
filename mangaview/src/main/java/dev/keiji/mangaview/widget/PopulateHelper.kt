package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Rectangle
import kotlin.math.sign

abstract class PopulateHelper {

    companion object {
        private val TAG = PopulateHelper::class.java.simpleName
    }

    internal lateinit var viewContext: ViewContext
    internal var layoutManager: LayoutManager? = null

    internal var pagingTouchSlop: Float = 0.0F

    internal var resetScaleOnPageChanged = true

    private var duration: Long = 0

    internal val calcDiffHorizontal =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            val rect = pageLayout.getScaledScrollArea(viewContext)
            val diffLeft = rect.left - viewContext.viewport.left
            val diffRight = rect.right - viewContext.viewport.right

            if (diffLeft.sign != diffRight.sign) {
                // no over-scroll
                return 0.0F
            }

            val overScrollLeft = diffLeft > 0
            val dx = if (overScrollLeft) {
                rect.left - viewContext.viewport.left
            } else {
                rect.right - viewContext.viewport.right
            }
            return dx
        }

    internal val calcDiffVertical =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            val rect = pageLayout.getScaledScrollArea(viewContext)
            val diffTop = rect.top - viewContext.viewport.top
            val diffBottom = rect.bottom - viewContext.viewport.bottom

            if (diffTop.sign != diffBottom.sign) {
                // no overflow
                return 0.0F
            }

            val overflowTop = diffTop > 0
            val dy = if (overflowTop) {
                rect.top - viewContext.viewport.top
            } else {
                rect.bottom - viewContext.viewport.bottom
            }
            return dy
        }

    internal val calcDiffX =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            return pageLayout.globalPosition.left - viewContext.viewport.left
        }

    internal val calcDiffY =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            return pageLayout.globalPosition.top - viewContext.viewport.top
        }

    private val tmp = Rectangle()

    fun init(
        viewContext: ViewContext,
        layoutManager: LayoutManager,
        pagingTouchSlop: Float,
        duration: Long,
        resetScaleOnPageChanged: Boolean = false
    ): PopulateHelper {
        this.viewContext = viewContext
        this.layoutManager = layoutManager
        this.pagingTouchSlop = pagingTouchSlop
        this.duration = duration
        this.resetScaleOnPageChanged = resetScaleOnPageChanged
        return this
    }

    fun populateTo(
        from: PageLayout?,
        to: PageLayout?,
        shouldPopulate: (Rectangle?) -> Boolean,
        dx: (ViewContext, PageLayout) -> Float,
        dy: (ViewContext, PageLayout) -> Float,
        scale: Float = viewContext.currentScale
    ): Animation? {
        from ?: return null
        to ?: return null

        val overlap = Rectangle.and(
            from.getScaledScrollArea(viewContext),
            viewContext.viewport,
            tmp
        )

        if (!shouldPopulate(overlap)) {
            return null
        }

        val startX = viewContext.viewport.left
        val startY = viewContext.viewport.top

        val dx = dx(viewContext, to)
        val dy = dy(viewContext, to)

        if (dx == 0.0F && dy == 0.0F) {
            return null
        }

        val destX = startX + dx
        val destY = startY + dy

        val operation = Animation(
            translate = Animation.Translate(
                startX, startY,
                destX, destY,
            ),
            durationMillis = duration
        )

        if (resetScaleOnPageChanged) {
            operation.scale = Animation.Scale(
                viewContext.currentScale,
                viewContext.minScale,
                null, null
            )
        }

        return operation
    }

    fun populateToCurrent(pageLayout: PageLayout?): Animation? {
        pageLayout ?: return null

        val startX = viewContext.viewport.left
        val startY = viewContext.viewport.top
        val dx = calcDiffHorizontal(viewContext, pageLayout)
        val dy = calcDiffVertical(viewContext, pageLayout)

        if (dx == 0.0F && dy == 0.0F) {
            return null
        }

        val destX = startX + dx
        val destY = startY + dy

        return Animation(
            translate = Animation.Translate(
                startX, startY,
                destX, destY,
            ),
            durationMillis = duration
        )
    }

    open fun populateToLeft(leftRect: PageLayout): Animation? {
        return null
    }

    open fun populateToRight(rightRect: PageLayout): Animation? {
        return null
    }

    open fun populateToTop(topRect: PageLayout): Animation? {
        return null
    }

    open fun populateToBottom(bottomRect: PageLayout): Animation? {
        return null
    }
}
