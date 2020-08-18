package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlin.math.roundToInt

class HorizontalPopulateHelper : PopulateHelper() {
    companion object {
        private val TAG = HorizontalPopulateHelper::class.java.simpleName
    }

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

        val leftRect = layoutManagerSnapshot.leftPageLayout(viewState)
        val rightRect = layoutManagerSnapshot.rightPageLayout(viewState)

        Log.d(TAG, "currentRect: $currentRect")
        Log.d(TAG, "leftRect: $leftRect")
        Log.d(TAG, "rightRect: $rightRect")

        val handled = populateTo(
            leftRect,
            shouldPopulateHorizontal,
            calcDiffXToLeft, calcDiffBlank
        ) or populateTo(
            rightRect,
            shouldPopulateHorizontal,
            calcDiffXToRight, calcDiffBlank
        )

        if (!handled) {
            Log.d(TAG, "tryPopulate to current")
            val matchHorizontalRect = arrayOf(leftRect, rightRect)
                .filterNotNull()
                .maxByOrNull {
                    Rectangle.jaccardIndex(it.position, viewState.viewport, populateTmp)
                }

            Log.d(TAG, "populate to current")
            matchHorizontalRect?.let {
                Log.d(TAG, "leftRect ${leftRect.toString()}")
                Log.d(TAG, "rightRect ${rightRect.toString()}")
                Log.d(TAG, "currentRect ${currentRect}")
                Log.d(TAG, "matchHorizontalRect $it")
            }

            val fromLeft = (matchHorizontalRect === leftRect)

            val calcDiffXToCurrent = if (fromLeft) {
                Log.d(TAG, "fromLeft ${viewState.viewport}")
                Log.d(TAG, "fromLeft ${currentRect}")
                calcDiffXToRight
            } else {
                Log.d(TAG, "fromRight ${viewState.viewport}")
                Log.d(TAG, "fromRight ${currentRect}")
                calcDiffXToLeft
            }

            populateTo(
                currentRect,
                shouldPopulateAlwaysTrue,
                calcDiffXToCurrent, calcDiffBlank
            )
        }
    }

    override fun populateToLeft(leftRect: PageLayout) {
        populateTo(
            leftRect,
            shouldPopulateHorizontal,
            calcDiffXToLeft, calcDiffBlank
        )
    }

    override fun populateToRight(rightRect: PageLayout) {
        populateTo(
            rightRect,
            shouldPopulateHorizontal,
            calcDiffXToRight, calcDiffBlank
        )
    }
}
