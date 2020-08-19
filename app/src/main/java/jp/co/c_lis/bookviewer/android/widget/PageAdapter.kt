package jp.co.c_lis.bookviewer.android.widget

abstract class PageAdapter {

    abstract val pageCount: Int

    abstract fun getPage(index: Int): Page
}
