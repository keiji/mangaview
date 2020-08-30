package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle

class HorizontalPopulateHelper : PopulateHelper() {
    companion object {
        private val TAG = HorizontalPopulateHelper::class.java.simpleName
    }

    private val shouldPopulateHorizontal = fun(rect: Rectangle?): Boolean {
        rect ?: return false
        val diff = viewContext.viewport.width - rect.width
        return diff > (pagingTouchSlop / viewContext.currentScale)
    }

    private val calcDiffX = fun(rect: Rectangle): Float {
        return rect.left
    }

    override fun populate(): Operation.Translate? {
        Log.d(TAG, "populate!")

        val layoutManagerSnapshot = layoutManager ?: return null

        val currentPageLayout = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentPageLayout
            ?.getScaledScrollArea(viewContext) ?: return null

        // detect over-scroll
        if (scrollArea.contains(viewContext.viewport)) {
            Log.d(TAG, "no over-scroll detected.")
            return null
        }

        val toLeft = (viewContext.viewport.centerX < scrollArea.centerX)

        val targetRect = if (toLeft) {
            layoutManagerSnapshot.leftPageLayout(viewContext)
        } else {
            layoutManagerSnapshot.rightPageLayout(viewContext)
        }

        val populateOperation = populateTo(
            scrollArea,
            targetRect?.globalPosition,
            shouldPopulateHorizontal,
            calcDiffX, calcDiffVertical
        )

        return if (populateOperation == null) {
            populateToCurrent(scrollArea)
        } else {
            null
        }
    }

    override fun populateToLeft(leftRect: PageLayout): Operation.Translate? {
        val layoutManagerSnapshot = layoutManager ?: return null

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect?.getScaledScrollArea(viewContext)

        return populateTo(
            scrollArea,
            leftRect.globalPosition,
            shouldPopulateHorizontal,
            calcDiffX, calcDiffVertical
        )
    }

    override fun populateToRight(rightRect: PageLayout): Operation.Translate? {
        val layoutManagerSnapshot = layoutManager ?: return null

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect?.getScaledScrollArea(viewContext)

        return populateTo(
            scrollArea,
            rightRect.globalPosition,
            shouldPopulateHorizontal,
            calcDiffX, calcDiffVertical
        )
    }
}
