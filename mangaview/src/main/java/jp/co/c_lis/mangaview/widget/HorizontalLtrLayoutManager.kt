package jp.co.c_lis.mangaview.widget

import kotlin.math.*

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
        viewState: ViewState
    ): Int = abs(viewState.viewport.centerX / viewState.viewWidth).toInt()

    override fun leftPageLayout(viewState: ViewState): PageLayout? {
        val leftIndex = currentPageLayoutIndex(viewState) - 1
        if (leftIndex < 0) {
            return null
        }
        return getPageLayout(leftIndex, viewState)

    }

    override fun rightPageLayout(viewState: ViewState): PageLayout? {
        val rightIndex = currentPageLayoutIndex(viewState) + 1
        if (rightIndex >= pageLayoutManager.getCount()) {
            return null
        }
        return getPageLayout(rightIndex, viewState)
    }

    override fun layout(index: Int, pageLayout: PageLayout, viewState: ViewState): PageLayout {
        val positionLeft = viewState.viewWidth * index

        pageLayout.position.also {
            it.left = positionLeft
            it.right = it.left + viewState.viewWidth
            it.top = 0.0F
            it.bottom = viewState.viewHeight
        }

        return pageLayout
    }

    override fun calcFirstVisiblePageLayoutIndex(viewState: ViewState): Int {
        return abs(floor(viewState.viewport.left / viewState.viewWidth)).toInt()
    }

    override fun calcLastVisiblePageLayoutIndex(viewState: ViewState): Int {
        return abs(ceil(viewState.viewport.left / viewState.viewWidth)).toInt()
    }
}

