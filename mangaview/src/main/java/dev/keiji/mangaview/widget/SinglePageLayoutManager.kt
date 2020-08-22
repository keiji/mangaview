package dev.keiji.mangaview.widget

class SinglePageLayoutManager : PageLayoutManager() {

    override fun getCount(pageCount: Int): Int {
        return pageCount
    }

    override fun calcPageLayoutIndex(pageIndex: Int) : Int = pageIndex

    override fun calcFirstPageIndex(pageLayoutIndex: Int): Int = pageLayoutIndex

    override fun calcLastPageIndex(pageLayoutIndex: Int): Int = pageLayoutIndex

    override fun createPageLayout(): PageLayout = SinglePageLayout()
}
