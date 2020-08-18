package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlin.math.roundToInt

class VerticalPopulateHelper : PopulateHelper() {

    companion object {
        private val TAG = VerticalPopulateHelper::class.java.simpleName
    }

    private val calcDiffBlank = fun(_: Rectangle) = 0

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

    override fun populate() {
        Log.d(TAG, "populate!")

        val layoutManagerSnapshot = layoutManager ?: return

        // detect overscroll
        val currentRect = layoutManagerSnapshot.currentPageLayout(viewState)
        if (currentRect.position.contains(viewState.viewport)) {
            Log.d(TAG, "not overscrolled.")
            return
        }

        val topRect = layoutManagerSnapshot.topPageLayout(viewState)
        val bottomRect = layoutManagerSnapshot.bottomPageLayout(viewState)

        Log.d(TAG, "currentRect: $currentRect")
        Log.d(TAG, "topRect: $topRect")
        Log.d(TAG, "bottomRect: $bottomRect")

        val handled = populateTo(
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
            val matchVerticalRect = arrayOf(topRect, bottomRect)
                .filterNotNull()
                .maxBy {
                    Rectangle.jaccardIndex(it.position, viewState.viewport, populateTmp)
                }

            Log.d(TAG, "populate to current")
            matchVerticalRect?.let {
                Log.d(TAG, "topRect ${topRect.toString()}")
                Log.d(TAG, "bottomRect ${bottomRect.toString()}")
                Log.d(TAG, "currentRect ${currentRect}")
                Log.d(TAG, "matchVerticalRect $it")
            }

            val fromTop = (matchVerticalRect === topRect)

            val calcDiffYToCurrent = if (fromTop) {
                calcDiffYToBottom
            } else {
                calcDiffYToTop
            }

            populateTo(
                currentRect,
                shouldPopulateAlwaysTrue,
                calcDiffBlank, calcDiffYToCurrent
            )
        }
    }

    override fun populateToTop(topRect: PageLayout) {
        populateTo(
            topRect,
            shouldPopulateVertical,
            calcDiffBlank, calcDiffYToTop
        )
    }

    override fun populateToBottom(bottomRect: PageLayout) {
        populateTo(
            bottomRect,
            shouldPopulateVertical,
            calcDiffBlank, calcDiffYToBottom
        )
    }

}
