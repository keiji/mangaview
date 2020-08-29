package dev.keiji.mangaview.widget

import kotlin.math.floor

class DoublePageStartOneSideLayoutManager(
    private val isSpread: Boolean = true
) : PageLayoutManager() {

    override fun getCount(pageCount: Int): Int {
        val totalPageCount = pageCount + 1
        return totalPageCount / 2 + (totalPageCount % 2)
    }

    override fun calcPageLayoutIndex(pageIndex: Int): Int {
        val index = pageIndex + 1
        return floor(index / 2.0).toInt()
    }

    override fun calcFirstPageIndex(pageLayoutIndex: Int): Int {
        return when (pageLayoutIndex) {
            0 -> -1
            1 -> 1
            else -> (pageLayoutIndex - 1) * 2 + 1
        }
    }

    override fun calcLastPageIndex(pageLayoutIndex: Int): Int {
        return when (pageLayoutIndex) {
            0 -> 0
            1 -> 2
            else -> pageLayoutIndex * 2
        }
    }

    override fun createPageLayout(index: Int): PageLayout =
        DoublePageLayout(index, isSpread)
}
