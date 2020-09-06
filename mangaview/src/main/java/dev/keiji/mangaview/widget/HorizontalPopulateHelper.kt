package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Rectangle

class HorizontalPopulateHelper : PopulateHelper() {
    companion object {
        private val TAG = HorizontalPopulateHelper::class.java.simpleName
    }

    private val calcDestRectangleToLeft =
        fun(
            viewContext: ViewContext,
            destPageLayout: PageLayout,
            scale: Float,
            tmp: Rectangle
        ): Rectangle {
            val vc = if (viewContext.currentScale == scale) {
                viewContext
            } else {
                viewContext.copy().also {
                    it.scaleTo(scale, viewContext.currentX, viewContext.currentY)
                }
            }

            val scrollableArea = destPageLayout.getScaledScrollArea(vc)
            val offsetX = scrollableArea.right - vc.viewport.right

            tmp.copyFrom(vc.viewport)
            tmp.offset(offsetX, 0.0F)

            return tmp
        }

    private val calcDestRectangleToRight =
        fun(
            viewContext: ViewContext,
            destPageLayout: PageLayout,
            scale: Float,
            tmp: Rectangle
        ): Rectangle {
            val vc = if (viewContext.currentScale == scale) {
                viewContext
            } else {
                viewContext.copy().also {
                    it.scaleTo(scale, viewContext.currentX, viewContext.currentY)
                }
            }

            val scrollableArea = destPageLayout.getScaledScrollArea(vc)
            val offsetX = scrollableArea.left - vc.viewport.left

            tmp.copyFrom(vc.viewport)
            tmp.offset(offsetX, 0.0F)

            return tmp
        }

    private val shouldPopulateHorizontal = fun(rect: Rectangle?): Boolean {
        rect ?: return false
        val diff = viewContext.viewport.width - rect.width
        return diff > (pagingTouchSlop / viewContext.currentScale)
    }

    override fun populateToLeft(leftRect: PageLayout, scale: Float): Animator? {
        val layoutManagerSnapshot = layoutManager ?: return null

        return populateTo(
            layoutManagerSnapshot.currentPageLayout(viewContext),
            leftRect,
            shouldPopulateHorizontal,
            calcDestRectangleToLeft,
            scale
        )
    }

    override fun populateToRight(rightRect: PageLayout, scale: Float): Animator? {
        val layoutManagerSnapshot = layoutManager ?: return null

        return populateTo(
            layoutManagerSnapshot.currentPageLayout(viewContext),
            rightRect,
            shouldPopulateHorizontal,
            calcDestRectangleToRight,
            scale
        )
    }
}
