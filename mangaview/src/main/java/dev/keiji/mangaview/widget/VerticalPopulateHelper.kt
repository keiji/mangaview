package dev.keiji.mangaview.widget

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

    private val calcDiffXToTop = fun(pageLayout: PageLayout): Float {
        return pageLayout.getScaledScrollArea(viewContext).top - viewContext.viewport.top
    }

    private val calcDiffXToBottom = fun(pageLayout: PageLayout): Float {
        return pageLayout.getScaledScrollArea(viewContext).bottom - viewContext.viewport.bottom
    }

    private val calcDiffYToGlobalTop = fun(pageLayout: PageLayout): Float {
        return pageLayout.globalPosition.top - viewContext.viewport.top
    }

    private val calcDiffYToGlobalBottom = fun(pageLayout: PageLayout): Float {
        return pageLayout.globalPosition.top - viewContext.viewport.top
    }

    override fun populateToTop(topRect: PageLayout): Operation? {
        val layoutManagerSnapshot = layoutManager ?: return null

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect?.getScaledScrollArea(viewContext) ?: return null

        val dx = if (resetScaleOnPageChanged) {
            calcDiffX
        } else {
            calcDiffHorizontal
        }
        val dy = if (resetScaleOnPageChanged) {
            calcDiffYToGlobalTop
        } else {
            calcDiffXToTop
        }

        return populateTo(
            scrollArea,
            topRect,
            shouldPopulateVertical,
            dx, dy,
        )
    }

    override fun populateToBottom(bottomRect: PageLayout): Operation? {
        val layoutManagerSnapshot = layoutManager ?: return null

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewContext)
        val scrollArea = currentRect?.getScaledScrollArea(viewContext) ?: return null

        val dx = if (resetScaleOnPageChanged) {
            calcDiffX
        } else {
            calcDiffHorizontal
        }
        val dy = if (resetScaleOnPageChanged) {
            calcDiffYToGlobalBottom
        } else {
            calcDiffXToBottom
        }

        return populateTo(
            scrollArea,
            bottomRect,
            shouldPopulateVertical,
            dx, dy,
        )
    }

}
