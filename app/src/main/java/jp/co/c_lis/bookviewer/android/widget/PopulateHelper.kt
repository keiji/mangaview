package jp.co.c_lis.bookviewer.android.widget

import android.widget.OverScroller
import jp.co.c_lis.bookviewer.android.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlin.math.roundToInt

abstract class PopulateHelper {

    companion object {
        private val TAG = PopulateHelper::class.java.simpleName
    }

    var scrollDuration: Int = 250

    lateinit var viewState: ViewState
    var layoutManager: LayoutManager? = null

    lateinit var settleScroller: OverScroller

    var pagingTouchSlop: Float = 0.0F

    val populateTmp = Rectangle()

    fun init(
        viewState: ViewState,
        layoutManager: LayoutManager,
        settleScroller: OverScroller,
        pagingTouchSlop: Float,
        scrollDuration: Int
    ): PopulateHelper {
        this.viewState = viewState
        this.layoutManager = layoutManager
        this.settleScroller = settleScroller
        this.pagingTouchSlop = pagingTouchSlop
        this.scrollDuration = scrollDuration
        return this
    }

    abstract fun populate()

    fun populateTo(
        pageLayout: PageLayout?,
        shouldPopulate: (Rectangle?) -> Boolean,
        dx: (Rectangle) -> Int,
        dy: (Rectangle) -> Int
    ): Boolean {
        pageLayout ?: return false

        val overlap = Rectangle.and(pageLayout.position, viewState.viewport, populateTmp)

        if (shouldPopulate(overlap)) {
            val startX = viewState.viewport.left.roundToInt()
            val startY = viewState.viewport.top.roundToInt()

            settleScroller.startScroll(
                startX,
                startY,
                dx(populateTmp),
                dy(populateTmp),
                scrollDuration
            )
            return true
        }

        return false
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
