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

    val tmpLeftScrollArea = Rectangle()
    val tmpRightScrollArea = Rectangle()
    val tmpTopScrollArea = Rectangle()
    val tmpBottomScrollArea = Rectangle()

    val calcDiffHorizontal = fun(rect: Rectangle): Float {
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

    val calcDiffVertical = fun(rect: Rectangle): Float {
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

    val tmp = Rectangle()

    fun init(
        viewContext: ViewContext,
        layoutManager: LayoutManager,
        pagingTouchSlop: Float,
    ): PopulateHelper {
        this.viewContext = viewContext
        this.layoutManager = layoutManager
        this.pagingTouchSlop = pagingTouchSlop
        return this
    }

    abstract fun populate(): Operation.Translate?

    fun populateTo(
        fromArea: Rectangle?,
        toArea: Rectangle?,
        shouldPopulate: (Rectangle?) -> Boolean,
        dx: (Rectangle) -> Float,
        dy: (Rectangle) -> Float
    ): Operation.Translate? {
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

        return Operation.Translate(
            startX, startY,
            dx, dy,
        )
    }

    fun populateToCurrent(area: Rectangle): Operation.Translate? {
        val startX = viewContext.viewport.left
        val startY = viewContext.viewport.top
        val dx = calcDiffHorizontal(area)
        val dy = calcDiffVertical(area)

        if (dx == 0.0F && dy == 0.0F) {
            return null
        }

        val destX = startX + dx
        val destY = startY + dy

        return Operation.Translate(
            startX, startY,
            destX, destY,
        )
    }

    open fun populateToLeft(leftRect: PageLayout): Operation.Translate? {
        return null
    }

    open fun populateToRight(rightRect: PageLayout): Operation.Translate? {
        return null
    }

    open fun populateToTop(topRect: PageLayout): Operation.Translate? {
        return null
    }

    open fun populateToBottom(bottomRect: PageLayout): Operation.Translate? {
        return null
    }
}
