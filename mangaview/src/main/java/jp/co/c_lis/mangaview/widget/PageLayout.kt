package jp.co.c_lis.mangaview.widget

import jp.co.c_lis.mangaview.Rectangle

abstract class PageLayout {

    val position = Rectangle()

    val scrollArea = Rectangle()

    abstract val isFilled: Boolean

    abstract fun add(page: Page)

    abstract val pages: List<Page>

    open fun flip(): PageLayout { return this }

    abstract fun initScrollArea()

    abstract fun calcScrollArea(rectangle: Rectangle, scale: Float): Rectangle
}
