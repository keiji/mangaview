package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Rectangle

abstract class PageLayout {
    val position = Rectangle()

    val scrollArea = Rectangle()

    val populateAreaLeft = Rectangle()
    val populateAreaTop = Rectangle()
    val populateAreaRight = Rectangle()
    val populateAreaBottom = Rectangle()

    abstract val isFilled: Boolean

    abstract fun add(page: Page)

    abstract val pages: List<Page>

    open fun flip() {}

    abstract fun calcScrollArea(rectangle: Rectangle, scale: Float): Rectangle
}
