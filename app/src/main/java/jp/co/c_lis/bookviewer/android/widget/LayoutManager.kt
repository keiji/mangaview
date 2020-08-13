package jp.co.c_lis.bookviewer.android.widget

abstract class LayoutManager {
    internal var pageList: List<Page> = ArrayList()

    abstract fun layout(viewState: ViewState)
}
