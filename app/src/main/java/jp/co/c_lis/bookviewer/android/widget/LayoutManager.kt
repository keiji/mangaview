package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Rectangle

abstract class LayoutManager {

    internal var pageList: List<Page> = ArrayList()

    abstract fun currentPageIndex(
        viewState: ViewState
    ): Int

    abstract fun currentRect(
        viewState: ViewState
    ): Rectangle

    open fun leftRect(viewState: ViewState): Rectangle? = null
    open fun rightRect(viewState: ViewState): Rectangle? = null
    open fun topRect(viewState: ViewState): Rectangle? = null
    open fun bottomRect(viewState: ViewState): Rectangle? = null

    abstract fun getPageRect(pageIndex: Int): Rectangle

    abstract fun visiblePages(
        viewState: ViewState,
        resultList: ArrayList<Page> = ArrayList(),
        offsetScreenPageLimit: Int = 1
    ): List<Page>

    abstract fun layout(viewState: ViewState)

    abstract fun calcPopulateY(viewState: ViewState, rect: Rectangle): Float
}
