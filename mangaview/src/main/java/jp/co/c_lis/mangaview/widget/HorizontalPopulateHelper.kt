package jp.co.c_lis.mangaview.widget

import jp.co.c_lis.mangaview.Log
import jp.co.c_lis.mangaview.Rectangle
import kotlin.math.roundToInt

class HorizontalPopulateHelper : PopulateHelper() {
    companion object {
        private val TAG = HorizontalPopulateHelper::class.java.simpleName
    }

    private val shouldPopulateHorizontal = fun(rect: Rectangle?): Boolean {
        rect ?: return false
        val diff = viewContext.viewport.width - rect.width
        return diff > (pagingTouchSlop / viewContext.currentScale)
    }

    private val calcDiffXToLeft = fun(rect: Rectangle): Int {
        return (rect.right - viewContext.viewport.right).roundToInt()
    }
    private val calcDiffXToRight = fun(rect: Rectangle): Int {
        return (rect.left - viewContext.viewport.left).roundToInt()
    }

    override fun populate() {
        Log.d(TAG, "populate!")

        val layoutManagerSnapshot = layoutManager ?: return

        val currentPageLayout = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentPageLayout
            ?.getScaledScrollArea(viewContext.currentScale) ?: return

        // detect overscroll
        if (scrollArea.contains(viewContext.viewport)) {
            Log.d(TAG, "no overscroll detected.")
            return
        }

        val toLeft = (viewContext.viewport.centerX < scrollArea.centerX)

        val handled = if (toLeft) {
            val leftRect = layoutManagerSnapshot.leftPageLayout(viewContext)
            val leftArea =
                leftRect?.calcScrollArea(
                    tmpLeftScrollArea,
                    viewContext.currentScale
                )
            populateTo(
                scrollArea,
                leftArea,
                shouldPopulateHorizontal,
                calcDiffXToLeft, calcDiffVertical,
                populateDuration
            )
        } else {
            val rightRect = layoutManagerSnapshot.rightPageLayout(viewContext)
            val rightArea =
                rightRect?.calcScrollArea(
                    tmpRightScrollArea,
                    viewContext.currentScale
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

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect
            ?.getScaledScrollArea(viewContext.currentScale)

        populateTo(
            scrollArea,
            leftRect.calcScrollArea(tmpLeftScrollArea, viewContext.currentScale),
            shouldPopulateHorizontal,
            calcDiffXToLeft, calcDiffVertical,
            populateDuration
        )
    }

    override fun populateToRight(rightRect: PageLayout) {
        val layoutManagerSnapshot = layoutManager ?: return

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect
            ?.getScaledScrollArea(viewContext.currentScale)

        populateTo(
            scrollArea,
            rightRect.calcScrollArea(
                tmpRightScrollArea, viewContext.currentScale
            ),
            shouldPopulateHorizontal,
            calcDiffXToRight, calcDiffVertical,
            populateDuration
        )
    }
}
