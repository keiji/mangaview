package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle


class VerticalPopulateHelper : PopulateHelper() {

    companion object {
        private val TAG = VerticalPopulateHelper::class.java.simpleName
    }

    private val shouldPopulateVertical = fun(rect: Rectangle?): Boolean {
        rect ?: return false
        val diff = viewContext.viewport.height - rect.height
        return diff > (pagingTouchSlop / viewContext.currentScale)
    }

    private val calcDiffY = fun(rect: Rectangle): Float {
        return rect.top
    }

    override fun populate(): Operation.Translate? {
        Log.d(TAG, "populate!")

        val layoutManagerSnapshot = layoutManager ?: return null

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect
            ?.getScaledScrollArea(viewContext) ?: return null

        // detect over-scroll
        if (scrollArea.contains(viewContext.viewport)) {
            Log.d(TAG, "no over-scroll detected.")
            return null
        }

        val toTop = (viewContext.viewport.centerY < scrollArea.centerY)

        val targetRect = if (toTop) {
            layoutManagerSnapshot.topPageLayout(viewContext)
        } else {
            layoutManagerSnapshot.bottomPageLayout(viewContext)
        }

        val populateOperation = populateTo(
            scrollArea,
            targetRect?.scrollArea,
            shouldPopulateVertical,
            calcDiffHorizontal, calcDiffY,
        )

        return if (populateOperation == null) {
            populateToCurrent(
                scrollArea,
            )
        } else {
            null
        }
    }

    override fun populateToTop(topRect: PageLayout): Operation.Translate? {
        val layoutManagerSnapshot = layoutManager ?: return null

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect?.getScaledScrollArea(viewContext) ?: return null

        return populateTo(
            scrollArea,
            topRect.globalPosition,
            shouldPopulateVertical,
            calcDiffHorizontal, calcDiffY,
        )
    }

    override fun populateToBottom(bottomRect: PageLayout): Operation.Translate? {
        val layoutManagerSnapshot = layoutManager ?: return null

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect?.getScaledScrollArea(viewContext) ?: return null

        return populateTo(
            scrollArea,
            bottomRect.globalPosition,
            shouldPopulateVertical,
            calcDiffHorizontal, calcDiffY,
        )
    }

}
