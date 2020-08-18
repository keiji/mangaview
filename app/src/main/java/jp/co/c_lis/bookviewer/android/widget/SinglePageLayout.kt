package jp.co.c_lis.bookviewer.android.widget

class SinglePageLayout : PageLayout() {
    override val isFilled: Boolean
        get() = page != null

    var page: Page? = null

    override fun add(page: Page) {
        page.also {
            it.position.set(position)
        }
        this.page = page
    }

    override val pages: List<Page>
        get() {
            val pageSnapshot = page ?: return emptyList()
            return listOf(pageSnapshot)
        }
}
