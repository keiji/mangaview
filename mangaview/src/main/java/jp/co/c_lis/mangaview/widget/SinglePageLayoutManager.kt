package jp.co.c_lis.mangaview.widget

class SinglePageLayoutManager : PageLayoutManager() {

    override fun getCount(pageCount: Int): Int {
        return pageCount
    }

    override fun calcFirstPageIndex(index: Int): Int = index

    override fun calcLastPageIndex(index: Int): Int = index

    override fun createPageLayout(): PageLayout = SinglePageLayout()
}
