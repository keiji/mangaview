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

    private val calcDestRectangleToTop =
        fun(
            viewContext: ViewContext,
            scaledViewContext: ViewContext,
            scrollableArea: Rectangle,
            tmp: Rectangle
        ): Rectangle {
            val offsetY = scrollableArea.bottom - viewContext.viewport.bottom

            tmp.copyFrom(scaledViewContext.viewport)
                .offset(0.0F, offsetY)

            return tmp
        }

    private val calcDestRectangleToBottom =
        fun(
            viewContext: ViewContext,
            scaledViewContext: ViewContext,
            scrollableArea: Rectangle,
            tmp: Rectangle
        ): Rectangle {
            val offsetY = scrollableArea.top - viewContext.viewport.top

            tmp.copyFrom(scaledViewContext.viewport)
                .offset(0.0F, offsetY)

            return tmp
        }

    override fun populateToTop(topRect: PageLayout, scale: Float): Animator? {
        val layoutManagerSnapshot = layoutManager ?: return null

        return populateTo(
            layoutManagerSnapshot.currentPageLayout(viewContext),
            topRect,
            shouldPopulateVertical,
            calcDestRectangleToTop,
            scale
        )
    }

    override fun populateToBottom(bottomRect: PageLayout, scale: Float): Animator? {
        val layoutManagerSnapshot = layoutManager ?: return null

        return populateTo(
            layoutManagerSnapshot.currentPageLayout(viewContext),
            bottomRect,
            shouldPopulateVertical,
            calcDestRectangleToBottom,
            scale
        )
    }

}
