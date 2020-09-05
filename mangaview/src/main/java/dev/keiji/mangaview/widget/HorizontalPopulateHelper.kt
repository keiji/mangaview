package dev.keiji.mangaview.widget

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

    private val calcDiffXToLeft =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            return pageLayout.getScaledScrollArea(viewContext).right - viewContext.viewport.right
        }

    private val calcDiffXToRight =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            return pageLayout.getScaledScrollArea(viewContext).left - viewContext.viewport.left
        }

    private val calcDiffXToGlobalLeft =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            return pageLayout.globalPosition.left - viewContext.viewport.left
        }

    private val calcDiffXToGlobalRight =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            return pageLayout.globalPosition.left - viewContext.viewport.left
        }

    override fun populateToLeft(leftRect: PageLayout): Animation? {
        val layoutManagerSnapshot = layoutManager ?: return null

        val dx = if (resetScaleOnPageChanged) {
            calcDiffXToGlobalLeft
        } else {
            calcDiffXToLeft
        }
        val dy = if (resetScaleOnPageChanged) {
            calcDiffY
        } else {
            calcDiffVertical
        }

        return populateTo(
            layoutManagerSnapshot.currentPageLayout(viewContext),
            leftRect,
            shouldPopulateHorizontal,
            dx, dy
        )
    }

    override fun populateToRight(rightRect: PageLayout): Animation? {
        val layoutManagerSnapshot = layoutManager ?: return null

        val dx = if (resetScaleOnPageChanged) {
            calcDiffXToGlobalRight
        } else {
            calcDiffXToRight
        }
        val dy = if (resetScaleOnPageChanged) {
            calcDiffY
        } else {
            calcDiffVertical
        }

        return populateTo(
            layoutManagerSnapshot.currentPageLayout(viewContext),
            rightRect,
            shouldPopulateHorizontal,
            dx, dy
        )
    }
}
