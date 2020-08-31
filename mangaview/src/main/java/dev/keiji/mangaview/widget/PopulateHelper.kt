package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Rectangle
import kotlin.math.sign

abstract class PopulateHelper {

    companion object {
        private val TAG = PopulateHelper::class.java.simpleName
    }

    lateinit var viewContext: ViewContext
    var layoutManager: LayoutManager? = null

    var pagingTouchSlop: Float = 0.0F

    var duration: Long = 0

    var resetScaleOnPageChanged = true

    val calcDiffHorizontal = fun(pageLayout: PageLayout): Float {
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

    val calcDiffVertical = fun(pageLayout: PageLayout): Float {
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

    val calcDiffX = fun(pageLayout: PageLayout): Float {
        return pageLayout.globalPosition.left - viewContext.viewport.left
    }

    val calcDiffY = fun(pageLayout: PageLayout): Float {
        return pageLayout.globalPosition.top - viewContext.viewport.top
    }

    val tmp = Rectangle()

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
        fromArea: Rectangle?,
        toArea: PageLayout?,
        shouldPopulate: (Rectangle?) -> Boolean,
        dx: (PageLayout) -> Float,
        dy: (PageLayout) -> Float
    ): Operation? {
        fromArea ?: return null
        toArea ?: return null

        val overlap = Rectangle.and(fromArea, viewContext.viewport, tmp)

        if (!shouldPopulate(overlap)) {
            return null
        }

        val startX = viewContext.viewport.left
        val startY = viewContext.viewport.top
        val dx = dx(toArea)
        val dy = dy(toArea)

        if (dx == 0.0F && dy == 0.0F) {
            return null
        }

        val destX = startX + dx
        val destY = startY + dy

        val operation = Operation(
            translate = Operation.Translate(
                startX, startY,
                destX, destY,
            ),
            durationMillis = duration
        )

        if (resetScaleOnPageChanged) {
            operation.scale = Operation.Scale(
                viewContext.currentScale,
                viewContext.minScale,
                null, null
            )
        }

        return operation
    }

    fun populateToCurrent(pageLayout: PageLayout?): Operation? {
        pageLayout ?: return null

        val startX = viewContext.viewport.left
        val startY = viewContext.viewport.top
        val dx = calcDiffHorizontal(pageLayout)
        val dy = calcDiffVertical(pageLayout)

        if (dx == 0.0F && dy == 0.0F) {
            return null
        }

        val destX = startX + dx
        val destY = startY + dy

        return Operation(
            translate = Operation.Translate(
                startX, startY,
                destX, destY,
            ),
            durationMillis = duration
        )
    }

    open fun populateToLeft(leftRect: PageLayout): Operation? {
        return null
    }

    open fun populateToRight(rightRect: PageLayout): Operation? {
        return null
    }

    open fun populateToTop(topRect: PageLayout): Operation? {
        return null
    }

    open fun populateToBottom(bottomRect: PageLayout): Operation? {
        return null
    }
}
