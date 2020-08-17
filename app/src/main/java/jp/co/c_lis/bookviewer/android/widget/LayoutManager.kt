package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlin.math.max
import kotlin.math.min

abstract class LayoutManager {

    internal abstract val populateHelper: PopulateHelper

    internal var pageList: List<Page> = ArrayList()

    abstract fun currentPageIndex(
        viewState: ViewState
    ): Int

    fun currentRect(viewState: ViewState): Rectangle {
        return pageList[currentPageIndex(viewState)].position
    }

    open fun leftRect(viewState: ViewState): Rectangle? = null
    open fun rightRect(viewState: ViewState): Rectangle? = null
    open fun topRect(viewState: ViewState): Rectangle? = null
    open fun bottomRect(viewState: ViewState): Rectangle? = null

    fun getPageRect(pageIndex: Int): Rectangle {
        return pageList[pageIndex].position
    }

    fun visiblePages(
        viewState: ViewState,
        resultList: ArrayList<Page> = ArrayList(),
        offsetScreenPageLimit: Int = 1
    ): List<Page> {
        val firstVisiblePageIndex = calcFirstVisiblePageIndex(viewState)
        val endVisiblePageIndex = calcEndVisiblePageIndex(viewState)

        var startIndex = min(endVisiblePageIndex, firstVisiblePageIndex)
        var endIndex = max(endVisiblePageIndex, firstVisiblePageIndex)

        startIndex -= offsetScreenPageLimit
        endIndex += offsetScreenPageLimit

        startIndex = max(0, startIndex)
        endIndex = min(endIndex, pageList.size - 1)

        resultList.clear()

        (startIndex..endIndex).forEach { index ->
            resultList.add(pageList[index])
        }

        return resultList
    }

    abstract fun layout(viewState: ViewState)

    abstract fun calcFirstVisiblePageIndex(viewState: ViewState): Int

    abstract fun calcEndVisiblePageIndex(viewState: ViewState): Int
}
