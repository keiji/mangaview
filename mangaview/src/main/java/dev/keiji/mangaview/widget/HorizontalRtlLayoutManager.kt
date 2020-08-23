package dev.keiji.mangaview.widget

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class HorizontalRtlLayoutManager : LayoutManager() {

    companion object {
        private val TAG = HorizontalRtlLayoutManager::class.java.simpleName
    }

    override val populateHelper: PopulateHelper = HorizontalPopulateHelper()

    override val initialScrollX: Float
        get() = -viewWidth.toFloat()

    override val initialScrollY: Float
        get() = 0.0F

    override fun currentPageLayoutIndex(
        viewContext: ViewContext
    ): Int = abs(viewContext.viewport.centerX / viewContext.viewWidth).toInt()

    override fun leftPageLayout(viewContext: ViewContext): PageLayout? {
        val leftIndex = currentPageLayoutIndex(viewContext) + 1
        if (leftIndex >= pageLayoutManager.getCount()) {
            return null
        }
        return getPageLayout(leftIndex, viewContext)

    }

    override fun rightPageLayout(viewContext: ViewContext): PageLayout? {
        val rightIndex = currentPageLayoutIndex(viewContext) - 1
        if (rightIndex < 0) {
            return null
        }
        return getPageLayout(rightIndex, viewContext)
    }

    override fun layout(index: Int, pageLayout: PageLayout, viewContext: ViewContext): PageLayout {
        val positionLeft = viewContext.viewWidth * -(index + 1)

        pageLayout.globalPosition.also {
            it.left = positionLeft
            it.right = it.left + viewContext.viewWidth
            it.top = 0.0F
            it.bottom = viewContext.viewHeight
        }

        return pageLayout.flip()
    }

    override fun calcLastVisiblePageLayoutIndex(viewContext: ViewContext): Int {
        return abs(floor(viewContext.viewport.right / viewContext.viewWidth)).toInt()
    }

    override fun calcFirstVisiblePageLayoutIndex(viewContext: ViewContext): Int {
        return abs(ceil(viewContext.viewport.right / viewContext.viewWidth)).toInt()
    }
}

