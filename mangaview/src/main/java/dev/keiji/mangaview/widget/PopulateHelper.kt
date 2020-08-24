package dev.keiji.mangaview.widget

import android.widget.OverScroller
import dev.keiji.mangaview.Rectangle
import kotlin.math.roundToInt
import kotlin.math.sign

abstract class PopulateHelper {

    companion object {
        private val TAG = PopulateHelper::class.java.simpleName
    }

    var populateDuration: Int = 250
    var reverseScrollDuration: Int = 250

    lateinit var viewContext: ViewContext
    var layoutManager: LayoutManager? = null

    lateinit var settleScroller: OverScroller

    var pagingTouchSlop: Float = 0.0F

    val tmpLeftScrollArea = Rectangle()
    val tmpRightScrollArea = Rectangle()
    val tmpTopScrollArea = Rectangle()
    val tmpBottomScrollArea = Rectangle()

    val calcDiffHorizontal = fun(rect: Rectangle): Float {
        val diffLeft = rect.left - viewContext.viewport.left
        val diffRight = rect.right - viewContext.viewport.right

        if (diffLeft.sign != diffRight.sign) {
            // no overflow
            return 0.0F
        }

        val overflowLeft = diffLeft > 0
        val dx = if (overflowLeft) {
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
        settleScroller: OverScroller,
        pagingTouchSlop: Float,
        scrollDuration: Int,
        reverseScrollDuration: Int,
    ): PopulateHelper {
        this.viewContext = viewContext
        this.layoutManager = layoutManager
        this.settleScroller = settleScroller
        this.pagingTouchSlop = pagingTouchSlop
        this.populateDuration = scrollDuration
        this.reverseScrollDuration = reverseScrollDuration
        return this
    }

    abstract fun populate()

    fun populateTo(
        fromArea: Rectangle?,
        toArea: Rectangle?,
        shouldPopulate: (Rectangle?) -> Boolean,
        dx: (Rectangle) -> Float,
        dy: (Rectangle) -> Float,
        duration: Int
    ): Boolean {
        fromArea ?: return false
        toArea ?: return false

        val overlap = Rectangle.and(fromArea, viewContext.viewport, tmp)

        if (shouldPopulate(overlap)) {
            val startX = viewContext.viewport.left
            val startY = viewContext.viewport.top
            val dx = dx(toArea)
            val dy = dy(toArea)

            if (dx == 0.0F && dy == 0.0F) {
                return false
            }

            settleScroller.also {
                it.abortAnimation()
                it.startScroll(
                    startX.roundToInt(),
                    startY.roundToInt(),
                    dx.roundToInt(),
                    dy.roundToInt(),
                    duration
                )
            }

            return true
        }

        return false
    }

    fun populateToCurrent(area: Rectangle, duration: Int) {
        val startX = viewContext.viewport.left
        val startY = viewContext.viewport.top
        val dx = calcDiffHorizontal(area)
        val dy = calcDiffVertical(area)

        if (dx == 0.0F && dy == 0.0F) {
            return
        }

        settleScroller.also {
            it.abortAnimation()
            it.startScroll(
                startX.roundToInt(),
                startY.roundToInt(),
                dx.roundToInt(),
                dy.roundToInt(),
                duration
            )
        }
    }

    open fun populateToLeft(leftRect: PageLayout) {
    }

    open fun populateToRight(rightRect: PageLayout) {
    }

    open fun populateToTop(topRect: PageLayout) {
    }

    open fun populateToBottom(bottomRect: PageLayout) {
    }

}
