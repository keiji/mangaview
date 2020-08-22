package dev.keiji.mangaview.widget

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class HorizontalLtrLayoutManager : LayoutManager() {

    companion object {
        private val TAG = HorizontalLtrLayoutManager::class.java.simpleName
    }

    override val populateHelper: PopulateHelper = HorizontalPopulateHelper()

    override val initialScrollX: Float
        get() = 0.0F

    override val initialScrollY: Float
        get() = 0.0F

    override fun init() {
    }

    override fun currentPageLayoutIndex(
        viewContext: ViewContext
    ): Int = abs(viewContext.viewport.centerX / viewContext.viewWidth).toInt()

    override fun leftPageLayout(viewContext: ViewContext): PageLayout? {
        val leftIndex = currentPageLayoutIndex(viewContext) - 1
        if (leftIndex < 0) {
            return null
        }
        return getPageLayout(leftIndex, viewContext)

    }

    override fun rightPageLayout(viewContext: ViewContext): PageLayout? {
        val rightIndex = currentPageLayoutIndex(viewContext) + 1
        if (rightIndex >= pageLayoutManager.getCount()) {
            return null
        }
        return getPageLayout(rightIndex, viewContext)
    }

    override fun layout(index: Int, pageLayout: PageLayout, viewContext: ViewContext): PageLayout {
        val positionLeft = viewContext.viewWidth * index

        pageLayout.position.also {
            it.left = positionLeft
            it.right = it.left + viewContext.viewWidth
            it.top = 0.0F
            it.bottom = viewContext.viewHeight
        }

        return pageLayout
    }

    override fun calcFirstVisiblePageLayoutIndex(viewContext: ViewContext): Int {
        return abs(floor(viewContext.viewport.left / viewContext.viewWidth)).toInt()
    }

    override fun calcLastVisiblePageLayoutIndex(viewContext: ViewContext): Int {
        return abs(ceil(viewContext.viewport.left / viewContext.viewWidth)).toInt()
    }
}

