package jp.co.c_lis.bookviewer.android.widget

abstract class LayoutManager {

    internal var pageList: List<Page> = ArrayList()

    abstract fun visiblePages(
        viewState: ViewState,
        resultList: ArrayList<Page> = ArrayList()
    ): List<Page>

    abstract fun layout(viewState: ViewState)
}
