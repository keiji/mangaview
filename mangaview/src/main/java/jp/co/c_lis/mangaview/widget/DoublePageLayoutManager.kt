package jp.co.c_lis.mangaview.widget

import kotlin.math.floor

class DoublePageLayoutManager(
    private val isSpread: Boolean = true
) : PageLayoutManager() {


    override fun getCount(pageCount: Int): Int {
        return pageCount / 2 + (pageCount % 2)
    }

    override fun calcPageLayoutIndex(pageIndex: Int): Int {
        return floor(pageIndex / 2.0).toInt()
    }

    override fun calcFirstPageIndex(pageLayoutIndex: Int): Int {
        return pageLayoutIndex * 2
    }

    override fun calcLastPageIndex(pageLayoutIndex: Int): Int {
        return calcFirstPageIndex(pageLayoutIndex) + 1
    }

    override fun createPageLayout(): PageLayout = DoublePageLayout(isSpread)
}
