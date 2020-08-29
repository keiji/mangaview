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

    private val calcDiffYToTop = fun(rect: Rectangle): Float {
        return (rect.bottom - viewContext.viewport.bottom)
    }

    private val calcDiffYToBottom = fun(rect: Rectangle): Float {
        return (rect.top - viewContext.viewport.top)
    }

    override fun populate() {
        Log.d(TAG, "populate!")

        val layoutManagerSnapshot = layoutManager ?: return

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect
            ?.getScaledScrollArea(viewContext) ?: return

        // detect overscroll
        if (scrollArea.contains(viewContext.viewport)) {
            return
        }

        val toTop = (viewContext.viewport.centerY < scrollArea.centerY)

        val handled = if (toTop) {
            val topRect = layoutManagerSnapshot.topPageLayout(viewContext)
            val topArea =
                topRect?.calcScrollArea(
                    viewContext,
                    tmpTopScrollArea
                )
            populateTo(
                scrollArea,
                topArea,
                shouldPopulateVertical,
                calcDiffHorizontal, calcDiffYToTop,
                populateDuration
            )
        } else {
            val bottomRect = layoutManagerSnapshot.bottomPageLayout(viewContext)
            val bottomArea =
                bottomRect?.calcScrollArea(
                    viewContext,
                    tmpBottomScrollArea
                )
            populateTo(
                scrollArea,
                bottomArea,
                shouldPopulateVertical,
                calcDiffHorizontal, calcDiffYToBottom,
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

    override fun populateToTop(topRect: PageLayout) {
        val layoutManagerSnapshot = layoutManager ?: return

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect?.getScaledScrollArea(viewContext) ?: return

        populateTo(
            scrollArea,
            topRect.calcScrollArea(viewContext, tmpLeftScrollArea),
            shouldPopulateVertical,
            calcDiffHorizontal, calcDiffYToTop,
            populateDuration
        )
    }

    override fun populateToBottom(bottomRect: PageLayout) {
        val layoutManagerSnapshot = layoutManager ?: return

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect?.getScaledScrollArea(viewContext) ?: return

        populateTo(
            scrollArea,
            bottomRect.calcScrollArea(viewContext, tmpLeftScrollArea),
            shouldPopulateVertical,
            calcDiffHorizontal, calcDiffYToBottom,
            populateDuration
        )
    }

}
