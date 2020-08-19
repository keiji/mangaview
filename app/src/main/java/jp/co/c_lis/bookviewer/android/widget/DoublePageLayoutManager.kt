package jp.co.c_lis.bookviewer.android.widget

class DoublePageLayoutManager(
    private val isSpread: Boolean = true
) : PageLayoutManager() {

    override fun getCount(pageCount: Int): Int {
        return pageCount / 2 + (pageCount % 2)
    }

    override fun init(pageList: List<Page>): ArrayList<PageLayout> {
        val layoutCount = getCount(pageList.size)

        pageLayoutList.also {
            it.clear()
            it.addAll((0 until layoutCount).map { DoublePageLayout(isSpread) })
        }

        return pageLayoutList
    }
}
