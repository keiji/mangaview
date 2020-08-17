package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Rectangle
import kotlin.math.*

class HorizontalLtrLayoutManager : LayoutManager() {

    companion object {
        private val TAG = HorizontalLtrLayoutManager::class.java.simpleName
    }

    override val populateHelper: PopulateHelper = HorizontalPopulateHelper()

    override fun currentPageIndex(
        viewState: ViewState
    ): Int = abs(viewState.viewport.centerX / viewState.viewWidth).toInt()

    override fun leftRect(viewState: ViewState): Rectangle? {
        val leftIndex = currentPageIndex(viewState) - 1
        if (leftIndex < 0) {
            return null
        }
        return getPageRect(leftIndex)

    }

    override fun rightRect(viewState: ViewState): Rectangle? {
        val rightIndex = currentPageIndex(viewState) + 1
        if (rightIndex >= pageList.size) {
            return null
        }
        return getPageRect(rightIndex)
    }

    override fun layout(viewState: ViewState) {

        // layout pages
        for (index in pageList.indices) {
            val page = pageList[index]

            val positionLeft = viewState.viewWidth * index

            page.position.also {
                it.left = positionLeft
                it.right = it.left + viewState.viewWidth
                it.top = 0.0F
                it.bottom = viewState.viewHeight
            }
        }

        viewState.scrollableArea.also { area ->
            area.left = pageList.minBy { it.position.left }?.position?.left ?: 0.0F
            area.right = pageList.maxBy { it.position.right }?.position?.right ?: 0.0F
            area.top = pageList.minBy { it.position.top }?.position?.top ?: 0.0F
            area.bottom = pageList.maxBy { it.position.bottom }?.position?.bottom ?: 0.0F
        }

        viewState.offset(-viewState.viewWidth, 0.0F)
    }

    override fun calcFirstVisiblePageIndex(viewState: ViewState): Int {
        return abs(floor(viewState.viewport.left / viewState.viewWidth)).toInt()
    }

    override fun calcEndVisiblePageIndex(viewState: ViewState): Int {
        return abs(ceil(viewState.viewport.left / viewState.viewWidth)).toInt()
    }
}

