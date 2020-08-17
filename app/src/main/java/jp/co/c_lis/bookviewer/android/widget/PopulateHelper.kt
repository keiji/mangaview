package jp.co.c_lis.bookviewer.android.widget

import android.widget.OverScroller
import jp.co.c_lis.bookviewer.android.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlin.math.roundToInt

class PopulateHelper {
    companion object {
        private val TAG = PopulateHelper::class.java.simpleName

        private var instance: PopulateHelper? = null

        fun obtain(
            viewState: ViewState,
            pagingTouchSlop: Float,
            layoutManager: LayoutManager?,
            settleScroller: OverScroller,
            scrollDuration: Int
        ): PopulateHelper {
            val instanceSnapshot = instance ?: PopulateHelper()

            return instanceSnapshot.also {
                it.viewState = viewState
                it.pagingTouchSlop = pagingTouchSlop
                it.layoutManager = layoutManager
                it.settleScroller = settleScroller
                it.scrollDuration = scrollDuration
            }
        }
    }

    private var scrollDuration: Int = 250

    private lateinit var settleScroller: OverScroller
    private var layoutManager: LayoutManager? = null
    private lateinit var viewState: ViewState

    private var pagingTouchSlop: Float = 0.0F

    private val populateTmp = Rectangle()

    private val shouldPopulateHorizontal = fun(rect: Rectangle?): Boolean {
        rect ?: return false
        return rect.width > pagingTouchSlop / viewState.currentScale
    }

    private val calcDiffBlank = fun(_: Rectangle) = 0

    private val calcDiffXToLeft = fun(rect: Rectangle): Int {
        val result = -(viewState.viewport.width - rect.width).roundToInt()
        return result
    }
    private val calcDiffXToRight = fun(rect: Rectangle): Int {
        val result = (viewState.viewport.width - rect.width).roundToInt()
        return result
    }

    private val shouldPopulateVertical = fun(rect: Rectangle?): Boolean {
        rect ?: return false
        return rect.height > pagingTouchSlop / viewState.currentScale
    }

    private val calcDiffYToTop = fun(rect: Rectangle): Int {
        return -(viewState.viewport.height - rect.height).roundToInt()
    }
    private val calcDiffYToBottom = fun(rect: Rectangle): Int {
        return (viewState.viewport.height - rect.height).roundToInt()
    }

    private val shouldPopulateAlwaysTrue = fun(rect: Rectangle?) = true

    fun populate() {
        Log.d(TAG, "populate!")

        val layoutManagerSnapshot = layoutManager ?: return

        // detect overscroll
        val currentRect = layoutManagerSnapshot.currentRect(viewState)
        if (currentRect.contains(viewState.viewport)) {
            Log.d(TAG, "not overscrolled.")
            return
        }

        val leftRect = layoutManagerSnapshot.leftRect(viewState)
        val rightRect = layoutManagerSnapshot.rightRect(viewState)
        val topRect = layoutManagerSnapshot.topRect(viewState)
        val bottomRect = layoutManagerSnapshot.bottomRect(viewState)

        Log.d(TAG, "currentRect: $currentRect")
        Log.d(TAG, "leftRect: $leftRect")
        Log.d(TAG, "rightRect: $rightRect")
        Log.d(TAG, "topRect: $topRect")
        Log.d(TAG, "bottomRect: $bottomRect")

        val handled = populateTo(
            leftRect,
            shouldPopulateHorizontal,
            calcDiffXToLeft, calcDiffBlank
        ) or populateTo(
            rightRect,
            shouldPopulateHorizontal,
            calcDiffXToRight, calcDiffBlank
        ) or populateTo(
            topRect,
            shouldPopulateVertical,
            calcDiffBlank, calcDiffYToTop
        ) or populateTo(
            bottomRect,
            shouldPopulateVertical,
            calcDiffBlank, calcDiffYToBottom
        )

        if (!handled) {
            Log.d(TAG, "tryPopulate to current")
            val matchHorizontalRect = arrayOf(leftRect, rightRect)
                .filterNotNull()
                .maxBy {
                    Rectangle.jaccardIndex(it, viewState.viewport, populateTmp)
                }
            val matchVerticalRect = arrayOf(topRect, bottomRect)
                .filterNotNull()
                .maxBy {
                    Rectangle.jaccardIndex(it, viewState.viewport, populateTmp)
                }

            Log.d(TAG, "populate to current")
            matchHorizontalRect?.let {
                Log.d(TAG, "leftRect ${leftRect.toString()}")
                Log.d(TAG, "rightRect ${rightRect.toString()}")
                Log.d(TAG, "currentRect ${currentRect}")
                Log.d(TAG, "matchHorizontalRect $it")
            }

            val fromLeft = (matchHorizontalRect === leftRect)
            val fromTop = (matchVerticalRect === topRect)

            val calcDiffXToCurrent = if (fromLeft) {
                Log.d(TAG, "fromLeft ${viewState.viewport}")
                Log.d(TAG, "fromLeft ${currentRect}")
                calcDiffXToRight
            } else {
                Log.d(TAG, "fromRight ${viewState.viewport}")
                Log.d(TAG, "fromRight ${currentRect}")
                calcDiffXToLeft
            }
            val calcDiffYToCurrent = if (fromTop) {
                calcDiffYToTop
            } else {
                calcDiffYToBottom
            }

            populateTo(
                currentRect,
                shouldPopulateAlwaysTrue,
                calcDiffXToCurrent, calcDiffYToCurrent
            )
        }
    }

    private fun populateTo(
        rect: Rectangle?,
        shouldPopulate: (Rectangle?) -> Boolean,
        dx: (Rectangle) -> Int,
        dy: (Rectangle) -> Int
    ): Boolean {
        rect ?: return false

        val overlap = Rectangle.and(rect, viewState.viewport, populateTmp)

        Log.d(TAG, "overlap ${overlap}")
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

    fun populateToLeft(leftRect: Rectangle) {
        populateTo(
            leftRect,
            shouldPopulateHorizontal,
            calcDiffXToLeft, calcDiffBlank
        )
    }

    fun populateToRight(rightRect: Rectangle) {
        populateTo(
            rightRect,
            shouldPopulateHorizontal,
            calcDiffXToRight, calcDiffBlank
        )
    }

    fun populateToTop(topRect: Rectangle) {
        populateTo(
            topRect,
            shouldPopulateVertical,
            calcDiffBlank, calcDiffYToTop
        )
    }

    fun populateToBottom(bottomRect: Rectangle) {
        populateTo(
            bottomRect,
            shouldPopulateVertical,
            calcDiffBlank, calcDiffYToBottom
        )
    }

}
