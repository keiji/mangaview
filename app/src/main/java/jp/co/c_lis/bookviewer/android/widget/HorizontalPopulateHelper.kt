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
        val diff = viewState.viewport.width - rect.width
        return diff > (pagingTouchSlop / viewState.currentScale)
    }

    private val calcDiffXToLeft = fun(rect: Rectangle): Int {
        return (rect.right - viewState.viewport.right).roundToInt()
    }
    private val calcDiffXToRight = fun(rect: Rectangle): Int {
        return (rect.left - viewState.viewport.left).roundToInt()
    }

    override fun populate() {
        Log.d(TAG, "populate!")

        val layoutManagerSnapshot = layoutManager ?: return

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewState)
        val scrollArea = currentRect.calcScrollArea(
            tmpCurrentScrollArea,
            viewState.currentScale
        )

        // detect overscroll
        if (scrollArea.contains(viewState.viewport)) {
            Log.d(TAG, "no overscroll detected.")
            return
        }

        val toLeft = (viewState.viewport.centerX < tmpCurrentScrollArea.centerX)

        val handled = if (toLeft) {
            val leftRect = layoutManagerSnapshot.leftPageLayout(viewState)
            val leftArea =
                leftRect?.calcScrollArea(
                    tmpLeftScrollArea,
                    viewState.currentScale
                )
            populateTo(
                scrollArea,
                leftArea,
                shouldPopulateHorizontal,
                calcDiffXToLeft, calcDiffVertical,
                populateDuration
            )
        } else {
            val rightRect = layoutManagerSnapshot.rightPageLayout(viewState)
            val rightArea =
                rightRect?.calcScrollArea(
                    tmpRightScrollArea,
                    viewState.currentScale
                )
            populateTo(
                scrollArea,
                rightArea,
                shouldPopulateHorizontal,
                calcDiffXToRight, calcDiffVertical,
                populateDuration
            )
        }

        if (!handled) {
            populateToCurrent(
                scrollArea,
                reverseScrollDuration
            )
        }
    }

    override fun populateToLeft(leftRect: PageLayout) {
        val layoutManagerSnapshot = layoutManager ?: return

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewState)
        val scrollArea = currentRect.calcScrollArea(
            tmpCurrentScrollArea,
            viewState.currentScale
        )

        populateTo(
            scrollArea,
            leftRect.calcScrollArea(tmpLeftScrollArea, viewState.currentScale),
            shouldPopulateHorizontal,
            calcDiffXToLeft, calcDiffVertical,
            populateDuration
        )
    }

    override fun populateToRight(rightRect: PageLayout) {
        val layoutManagerSnapshot = layoutManager ?: return

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewState)
        val scrollArea = currentRect.calcScrollArea(
            tmpCurrentScrollArea,
            viewState.currentScale
        )

        populateTo(
            scrollArea,
            rightRect.calcScrollArea(
                tmpRightScrollArea, viewState.currentScale
            ),
            shouldPopulateHorizontal,
            calcDiffXToRight, calcDiffVertical,
            populateDuration
        )
    }
}
