package jp.co.c_lis.bookviewer.android.widget

import kotlin.math.*

class VerticalLayoutManager : LayoutManager() {

    companion object {
        private val TAG = VerticalLayoutManager::class.java.simpleName
    }

    override val populateHelper: PopulateHelper = VerticalPopulateHelper()

    override val initialScrollX: Float
        get() = 0.0F

    override val initialScrollY: Float
        get() = 0.0F

    override fun init() {
    }

    override fun currentPageLayoutIndex(
        viewState: ViewState
    ): Int = abs(viewState.viewport.centerY / viewState.viewHeight).toInt()

    override fun topPageLayout(viewState: ViewState): PageLayout? {
        val topIndex = currentPageLayoutIndex(viewState) - 1
        if (topIndex < 0) {
            return null
        }
        return getPageLayout(topIndex, viewState)

    }

    override fun bottomPageLayout(viewState: ViewState): PageLayout? {
        val bottomIndex = currentPageLayoutIndex(viewState) + 1
        if (bottomIndex >= pageLayoutManager.getCount()) {
            return null
        }
        return getPageLayout(bottomIndex, viewState)
    }

    override fun layout(index: Int, pageLayout: PageLayout, viewState: ViewState): PageLayout {
        val positionTop = viewState.viewHeight * index

        pageLayout.position.also {
            it.left = 0.0F
            it.right = viewState.viewWidth
            it.top = positionTop
            it.bottom = it.top + viewState.viewHeight
        }

        return pageLayout
    }

    override fun calcFirstVisiblePageLayoutIndex(viewState: ViewState): Int {
        return abs(floor(viewState.viewport.top / viewState.viewHeight)).toInt()
    }

    override fun calcLastVisiblePageLayoutIndex(viewState: ViewState): Int {
        return abs(ceil(viewState.viewport.top / viewState.viewHeight)).toInt()
    }
}

