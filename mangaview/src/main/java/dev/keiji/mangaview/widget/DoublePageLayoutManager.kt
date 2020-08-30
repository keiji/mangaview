package dev.keiji.mangaview.widget

import kotlin.math.floor

class DoublePageLayoutManager(
    private val isSpread: Boolean = true,
    private val startOneSide: Boolean = false
) : PageLayoutManager() {

    override fun getCount(pageCount: Int): Int {
        if (startOneSide) {
            val totalPageCount = pageCount + 1
            return totalPageCount / 2 + (totalPageCount % 2)
        }

        return pageCount / 2 + (pageCount % 2)
    }

    override fun calcPageLayoutIndex(pageIndex: Int): Int {
        if (startOneSide) {
            val index = pageIndex + 1
            return floor(index / 2.0).toInt()
        }

        return floor(pageIndex / 2.0).toInt()
    }

    override fun calcFirstPageIndex(pageLayoutIndex: Int): Int {
        if (startOneSide) {
            return when {
                pageLayoutIndex < 2 -> pageLayoutIndex
                else -> (pageLayoutIndex - 1) * 2 + 1
            }
        }

        return pageLayoutIndex * 2
    }

    override fun calcLastPageIndex(pageLayoutIndex: Int): Int {
        if (startOneSide) {
            return when (pageLayoutIndex) {
                0 -> 0
                1 -> 2
                else -> pageLayoutIndex * 2
            }
        }

        return calcFirstPageIndex(pageLayoutIndex) + 1
    }

    override fun createPageLayout(index: Int): PageLayout =
        DoublePageLayout(index, startOneSide, isSpread)
}
