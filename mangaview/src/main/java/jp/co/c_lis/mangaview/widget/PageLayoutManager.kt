package jp.co.c_lis.mangaview.widget

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

    abstract fun calcPageLayoutIndex(pageIndex: Int): Int

    abstract fun calcFirstPageIndex(pageLayoutIndex: Int): Int
    abstract fun calcLastPageIndex(pageLayoutIndex: Int): Int

    abstract fun createPageLayout(): PageLayout
}
