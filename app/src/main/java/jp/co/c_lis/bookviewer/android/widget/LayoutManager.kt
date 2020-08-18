package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Rectangle
import kotlin.math.max
import kotlin.math.min

abstract class LayoutManager {

    internal abstract val populateHelper: PopulateHelper

    internal var pageLayoutList: List<PageLayout> = ArrayList()
    internal var pageList: List<Page> = ArrayList()

    abstract fun currentPageLayoutIndex(
        viewState: ViewState
    ): Int

    fun currentPageLayout(viewState: ViewState): PageLayout {
        return pageLayoutList[currentPageLayoutIndex(viewState)]
    }

    open fun leftPageLayout(viewState: ViewState): PageLayout? = null
    open fun rightPageLayout(viewState: ViewState): PageLayout? = null
    open fun topPageLayout(viewState: ViewState): PageLayout? = null
    open fun bottomPageLayout(viewState: ViewState): PageLayout? = null

    fun getPageLayout(pageIndex: Int): PageLayout {
        return pageLayoutList[pageIndex]
    }

    fun visiblePages(
        viewState: ViewState,
        resultList: ArrayList<Page> = ArrayList(),
        offsetScreenPageLimit: Int = 1
    ): List<Page> {
        val firstVisiblePageLayoutIndex = calcFirstVisiblePageLayoutIndex(viewState)
        val endVisiblePageLayoutIndex = calcEndVisiblePageLayoutIndex(viewState)

        var startIndex = min(endVisiblePageLayoutIndex, firstVisiblePageLayoutIndex)
        var endIndex = max(endVisiblePageLayoutIndex, firstVisiblePageLayoutIndex)

        startIndex -= offsetScreenPageLimit
        endIndex += offsetScreenPageLimit

        startIndex = max(0, startIndex)
        endIndex = min(endIndex, pageLayoutList.size - 1)

        resultList.clear()

        (startIndex..endIndex).forEach { index ->
            val pageLayout = pageLayoutList[index]
            resultList.addAll(pageLayout.pages)
        }

        return resultList
    }

    abstract fun layout(viewState: ViewState, pageLayoutManager: PageLayoutManager)

    abstract fun calcFirstVisiblePageLayoutIndex(viewState: ViewState): Int

    abstract fun calcEndVisiblePageLayoutIndex(viewState: ViewState): Int
}
