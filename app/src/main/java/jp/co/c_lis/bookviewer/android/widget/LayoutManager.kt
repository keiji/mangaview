package jp.co.c_lis.bookviewer.android.widget

abstract class LayoutManager {

    internal var pageList: List<Page> = ArrayList()

    abstract fun currentPageIndex(
        viewState: ViewState
    ): Int

    fun currentPage(
        viewState: ViewState
    ): Page = pageList[currentPageIndex(viewState)]

    abstract fun visiblePages(
        viewState: ViewState,
        resultList: ArrayList<Page> = ArrayList(),
        offsetScreenPageLimit: Int = 1
    ): List<Page>

    abstract fun layout(viewState: ViewState)
}
