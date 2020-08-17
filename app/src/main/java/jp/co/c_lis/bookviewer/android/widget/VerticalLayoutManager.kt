package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlin.math.*

class VerticalLayoutManager : LayoutManager() {

    companion object {
        private val TAG = VerticalLayoutManager::class.java.simpleName
    }

    override val populateHelper: PopulateHelper = VerticalPopulateHelper()

    override fun currentPageIndex(
        viewState: ViewState
    ): Int = abs(viewState.viewport.centerY / viewState.viewHeight).toInt()

    override fun topRect(viewState: ViewState): Rectangle? {
        val topIndex = currentPageIndex(viewState) - 1
        if (topIndex < 0) {
            return null
        }
        return getPageRect(topIndex)

    }

    override fun bottomRect(viewState: ViewState): Rectangle? {
        val bottomIndex = currentPageIndex(viewState) + 1
        if (bottomIndex >= pageList.size) {
            return null
        }
        return getPageRect(bottomIndex)
    }

    override fun layout(viewState: ViewState) {

        // layout pages
        for (index in pageList.indices) {
            val page = pageList[index]

            val positionTop = viewState.viewHeight * index

            page.position.also {
                it.left = 0.0F
                it.right = viewState.viewWidth
                it.top = positionTop
                it.bottom = it.top + viewState.viewHeight
            }
        }

        viewState.scrollableArea.also { area ->
            area.left = pageList.minBy { it.position.left }?.position?.left ?: 0.0F
            area.right = pageList.maxBy { it.position.right }?.position?.right ?: 0.0F
            area.top = pageList.minBy { it.position.top }?.position?.top ?: 0.0F
            area.bottom = pageList.maxBy { it.position.bottom }?.position?.bottom ?: 0.0F
        }
    }

    override fun calcFirstVisiblePageIndex(viewState: ViewState): Int {
        return abs(floor(viewState.viewport.top / viewState.viewHeight)).toInt()
    }

    override fun calcEndVisiblePageIndex(viewState: ViewState): Int {
        return abs(ceil(viewState.viewport.top / viewState.viewHeight)).toInt()
    }
}

