package jp.co.c_lis.bookviewer.android.widget

abstract class PageLayoutManager {

    val pageLayoutList: ArrayList<PageLayout> = ArrayList()

    abstract fun getCount(pageCount: Int): Int

    abstract fun init(pageList: List<Page>): ArrayList<PageLayout>

    fun add(page: Page): PageLayout {
        val pageLayout = pageLayoutList.first { !it.isFilled }
        pageLayout.add(page)
        return pageLayout
    }
}
