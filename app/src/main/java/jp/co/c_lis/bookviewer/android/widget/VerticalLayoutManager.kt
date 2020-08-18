package jp.co.c_lis.bookviewer.android.widget

import kotlin.math.*

class VerticalLayoutManager : LayoutManager() {

    companion object {
        private val TAG = VerticalLayoutManager::class.java.simpleName
    }

    override val populateHelper: PopulateHelper = VerticalPopulateHelper()

    override fun currentPageLayoutIndex(
        viewState: ViewState
    ): Int = abs(viewState.viewport.centerY / viewState.viewHeight).toInt()

    override fun topPageLayout(viewState: ViewState): PageLayout? {
        val topIndex = currentPageLayoutIndex(viewState) - 1
        if (topIndex < 0) {
            return null
        }
        return getPageLayout(topIndex)

    }

    override fun bottomPageLayout(viewState: ViewState): PageLayout? {
        val bottomIndex = currentPageLayoutIndex(viewState) + 1
        if (bottomIndex >= pageLayoutList.size) {
            return null
        }
        return getPageLayout(bottomIndex)
    }

    override fun layout(viewState: ViewState, pageLayoutManager: PageLayoutManager) {

        pageLayoutList = pageLayoutManager.init(pageList)

        // layout pageContainer
        for (index in pageLayoutList.indices) {
            val pageContainer = pageLayoutList[index]

            val positionTop = viewState.viewHeight * index

            pageContainer.position.also {
                it.left = 0.0F
                it.right = viewState.viewWidth
                it.top = positionTop
                it.bottom = it.top + viewState.viewHeight
            }
        }

        // layout page
        pageList.forEach {
            pageLayoutManager.add(it).flip()
        }

        viewState.scrollableArea.also { area ->
            area.left = pageLayoutList.minByOrNull { it.position.left }?.position?.left ?: 0.0F
            area.right =
                pageLayoutList.maxByOrNull { it.position.right }?.position?.right ?: 0.0F
            area.top = pageLayoutList.minByOrNull { it.position.top }?.position?.top ?: 0.0F
            area.bottom =
                pageLayoutList.maxByOrNull { it.position.bottom }?.position?.bottom ?: 0.0F
        }
    }

    override fun calcFirstVisiblePageLayoutIndex(viewState: ViewState): Int {
        return abs(floor(viewState.viewport.top / viewState.viewHeight)).toInt()
    }

    override fun calcEndVisiblePageLayoutIndex(viewState: ViewState): Int {
        return abs(ceil(viewState.viewport.top / viewState.viewHeight)).toInt()
    }
}

