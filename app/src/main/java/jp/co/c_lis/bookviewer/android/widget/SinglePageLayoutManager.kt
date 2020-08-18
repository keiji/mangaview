package jp.co.c_lis.bookviewer.android.widget

class SinglePageLayoutManager : PageLayoutManager() {

    override fun getCount(pageCount: Int): Int {
        return pageCount
    }

    override fun init(pageList: List<Page>): ArrayList<PageLayout> {
        val containerCount = getCount(pageList.size)

        pageLayoutList.also {
            it.clear()
            it.addAll((0 until containerCount).map { SinglePageLayout() })
        }

        return pageLayoutList
    }
}
