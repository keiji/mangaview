package jp.co.c_lis.bookviewer.android.widget

import kotlin.math.*

class HorizontalLtrLayoutManager : LayoutManager() {

    companion object {
        private val TAG = HorizontalLtrLayoutManager::class.java.simpleName
    }

    override val populateHelper: PopulateHelper = HorizontalPopulateHelper()

    override fun currentPageLayoutIndex(
        viewState: ViewState
    ): Int = abs(viewState.viewport.centerX / viewState.viewWidth).toInt()

    override fun leftPageLayout(viewState: ViewState): PageLayout? {
        val leftIndex = currentPageLayoutIndex(viewState) - 1
        if (leftIndex < 0) {
            return null
        }
        return getPageLayout(leftIndex)

    }

    override fun rightPageLayout(viewState: ViewState): PageLayout? {
        val rightIndex = currentPageLayoutIndex(viewState) + 1
        if (rightIndex >= pageLayoutList.size) {
            return null
        }
        return getPageLayout(rightIndex)
    }

    override fun layout(viewState: ViewState, pageLayoutManager: PageLayoutManager) {

        pageLayoutList = pageLayoutManager.init(pageList)

        // layout pages
        for (index in pageLayoutList.indices) {
            val pageContainer = pageLayoutList[index]

            val positionLeft = viewState.viewWidth * index

            pageContainer.position.also {
                it.left = positionLeft
                it.right = it.left + viewState.viewWidth
                it.top = 0.0F
                it.bottom = viewState.viewHeight
            }
        }

        // layout page
        pageList.forEach {
            pageLayoutManager.add(it)
        }

        viewState.scrollableArea.also { area ->
            area.left = pageList.minByOrNull { it.position.left }?.position?.left ?: 0.0F
            area.right = pageList.maxByOrNull { it.position.right }?.position?.right ?: 0.0F
            area.top = pageList.minByOrNull { it.position.top }?.position?.top ?: 0.0F
            area.bottom = pageList.maxByOrNull { it.position.bottom }?.position?.bottom ?: 0.0F
        }

        viewState.offset(-viewState.viewWidth, 0.0F)
    }

    override fun calcFirstVisiblePageLayoutIndex(viewState: ViewState): Int {
        return abs(floor(viewState.viewport.left / viewState.viewWidth)).toInt()
    }

    override fun calcEndVisiblePageLayoutIndex(viewState: ViewState): Int {
        return abs(ceil(viewState.viewport.left / viewState.viewWidth)).toInt()
    }
}

