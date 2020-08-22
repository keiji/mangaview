package jp.co.c_lis.mangaview.widget

class DoublePageLayoutManager(
    private val isSpread: Boolean = true
) : PageLayoutManager() {


    override fun getCount(pageCount: Int): Int {
        return pageCount / 2 + (pageCount % 2)
    }

    override fun layout(pageLayout: PageLayout, index: Int): PageLayout {
        return super.layout(pageLayout, index).flip()
    }

    override fun calcFirstPageIndex(index: Int): Int {
        return index * 2
    }

    override fun calcLastPageIndex(index: Int): Int {
        return calcFirstPageIndex(index) + 1
    }

    override fun createPageLayout(): PageLayout = DoublePageLayout(isSpread)
}
