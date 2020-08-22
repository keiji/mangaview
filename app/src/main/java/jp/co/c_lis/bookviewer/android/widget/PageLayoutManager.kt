package jp.co.c_lis.bookviewer.android.widget

abstract class PageLayoutManager {

    lateinit var viewState: ViewState
    lateinit var pageAdapter: PageAdapter

    abstract fun getCount(pageCount: Int = pageAdapter.pageCount): Int

    open fun layout(pageLayout: PageLayout, index: Int): PageLayout {
        val firstPageIndex = calcFirstPageIndex(index)
        val lastPageIndex = calcLastPageIndex(index)

        (firstPageIndex..lastPageIndex).forEach { pageIndex ->
            pageLayout.add(pageAdapter.getPage(pageIndex))
        }

        return pageLayout
    }

    abstract fun calcFirstPageIndex(index: Int): Int
    abstract fun calcLastPageIndex(index: Int): Int

    abstract fun createPageLayout(): PageLayout
}
