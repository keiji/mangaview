package jp.co.c_lis.bookviewer.android.widget

class DoublePageLayoutManager : PageLayoutManager() {

    override fun getCount(pageCount: Int): Int {
        return pageCount / 2 + if (pageCount % 2 == 1) {
            1
        } else {
            0
        }
    }

    override fun init(pageList: List<Page>): ArrayList<PageLayout> {
        val containerCount = getCount(pageList.size)

        pageLayoutList.also {
            it.clear()
            it.addAll((0 until containerCount).map { DoublePageLayout() })
        }

        return pageLayoutList
    }
}
