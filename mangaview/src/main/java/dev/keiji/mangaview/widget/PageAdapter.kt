package dev.keiji.mangaview.widget

abstract class PageAdapter {

    abstract val pageCount: Int

    abstract fun getPageWidth(index: Int): Int

    abstract fun getPageHeight(index: Int): Int

    private fun createPage(index: Int) = Page(index, getPageWidth(index), getPageHeight(index))

    fun getPage(index: Int): Page {
        return createPage(index).also {
            onConstructPage(index, it)
        }
    }

    abstract fun onConstructPage(index: Int, page: Page)
}
