package jp.co.c_lis.mangaview.widget

import android.widget.OverScroller
import jp.co.c_lis.mangaview.Rectangle
import kotlin.math.roundToInt
import kotlin.math.sign

abstract class PopulateHelper {

    companion object {
        private val TAG = PopulateHelper::class.java.simpleName
    }

    var populateDuration: Int = 250
    var reverseScrollDuration: Int = 250

    lateinit var viewState: ViewState
    var layoutManager: LayoutManager? = null

    lateinit var settleScroller: OverScroller

    var pagingTouchSlop: Float = 0.0F

    val tmpCurrentScrollArea = Rectangle()

    val tmpLeftScrollArea = Rectangle()
    val tmpRightScrollArea = Rectangle()
    val tmpTopScrollArea = Rectangle()
    val tmpBottomScrollArea = Rectangle()

    val calcDiffHorizontal = fun(rect: Rectangle): Int {
        val diffLeft = rect.left - viewState.viewport.left
        val diffRight = rect.right - viewState.viewport.right

        if (diffLeft.sign != diffRight.sign) {
            // no overflow
            return 0
        }

        val overflowLeft = diffLeft > 0
        val dx = if (overflowLeft) {
            rect.left - viewState.viewport.left
        } else {
            rect.right - viewState.viewport.right
        }
        return dx.roundToInt()
    }

    val calcDiffVertical = fun(rect: Rectangle): Int {
        val diffTop = rect.top - viewState.viewport.top
        val diffBottom = rect.bottom - viewState.viewport.bottom

        if (diffTop.sign != diffBottom.sign) {
            // no overflow
            return 0
        }

        val overflowTop = diffTop > 0
        val dy = if (overflowTop) {
            rect.top - viewState.viewport.top
        } else {
            rect.bottom - viewState.viewport.bottom
        }
        return dy.roundToInt()
    }

    val tmp = Rectangle()

    fun init(
        viewState: ViewState,
        layoutManager: LayoutManager,
        settleScroller: OverScroller,
        pagingTouchSlop: Float,
        scrollDuration: Int,
        reverseScrollDuration: Int,
    ): PopulateHelper {
        this.viewState = viewState
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
        dx: (Rectangle) -> Int,
        dy: (Rectangle) -> Int,
        duration: Int
    ): Boolean {
        fromArea ?: return false
        toArea ?: return false

        val overlap = Rectangle.and(fromArea, viewState.viewport, tmp)

        if (shouldPopulate(overlap)) {
            val startX = viewState.viewport.left
            val startY = viewState.viewport.top
            settleScroller.startScroll(
                startX.roundToInt(),
                startY.roundToInt(),
                dx(toArea),
                dy(toArea),
                duration
            )
            return true
        }

        return false
    }

    fun populateToCurrent(area: Rectangle, scrollDuration: Int) {
        val overlap = Rectangle.and(area, viewState.viewport, tmp)
        overlap ?: return

        val startX = viewState.viewport.left.roundToInt()
        val startY = viewState.viewport.top.roundToInt()
        val dx =
            (overlap.left - viewState.viewport.left) + (overlap.right - viewState.viewport.right)
        val dy =
            (overlap.top - viewState.viewport.top) + (overlap.bottom - viewState.viewport.bottom)

        settleScroller.startScroll(
            startX,
            startY,
            dx.roundToInt(),
            dy.roundToInt(),
            scrollDuration
        )
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
