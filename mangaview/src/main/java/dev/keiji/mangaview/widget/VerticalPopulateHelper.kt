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

    private val calcDiffXToTop =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            return pageLayout.getScaledScrollArea(viewContext).top - viewContext.viewport.top
        }

    private val calcDiffXToBottom =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            return pageLayout.getScaledScrollArea(viewContext).bottom - viewContext.viewport.bottom
        }

    private val calcDiffYToGlobalTop =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            return pageLayout.globalPosition.top - viewContext.viewport.top
        }

    private val calcDiffYToGlobalBottom =
        fun(viewContext: ViewContext, pageLayout: PageLayout): Float {
            return pageLayout.globalPosition.top - viewContext.viewport.top
        }

    override fun populateToTop(topRect: PageLayout, scale: Float): Animator? {
        return null
    }

    override fun populateToBottom(bottomRect: PageLayout, scale: Float): Animator? {
        return null
    }

}
