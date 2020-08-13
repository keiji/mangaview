package jp.co.c_lis.bookviewer.android.widget

abstract class PageAdapter {

    abstract fun getPageCount(): Int

    abstract fun getPage(number: Int): Page
}
