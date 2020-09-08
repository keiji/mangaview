package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Rectangle
import dev.keiji.mangaview.layout.PageLayout

class HorizontalPopulateHelper : PopulateHelper() {
    companion object {
        private val TAG = HorizontalPopulateHelper::class.java.simpleName
    }

    private val shouldPopulateHorizontal = fun(rect: Rectangle?): Boolean {
        rect ?: return false

        val diff = viewContext.viewport.width - rect.width
        return diff > (pagingTouchSlop / viewContext.currentScale)
    }

    private val calcDestRectangleToLeft =
        fun(
            viewContext: ViewContext,
            scaledViewContext: ViewContext,
            scrollableArea: Rectangle,
            tmp: Rectangle
        ): Rectangle {
            val offsetX = scrollableArea.right - viewContext.viewport.right

            tmp.copyFrom(scaledViewContext.viewport)
                .offset(offsetX, 0.0F)

            return tmp
        }

    private val calcDestRectangleToRight =
        fun(
            viewContext: ViewContext,
            scaledViewContext: ViewContext,
            scrollableArea: Rectangle,
            tmp: Rectangle
        ): Rectangle {
            val offsetX = scrollableArea.left - viewContext.viewport.left

            tmp.copyFrom(scaledViewContext.viewport)
                .offset(offsetX, 0.0F)

            return tmp
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
