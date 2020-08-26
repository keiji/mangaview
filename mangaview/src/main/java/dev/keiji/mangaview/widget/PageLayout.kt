package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Rectangle
import kotlin.math.max

abstract class PageLayout {

    val globalPosition = Rectangle()

    val scrollArea = Rectangle()

    abstract val isFilled: Boolean

    abstract val keyPage: Page?

    abstract fun add(page: Page)

    abstract fun replace(targetPage: Page, newPage: Page?)

    fun remove(page: Page) {
        replace(page, null)
    }

    abstract val pages: List<Page>

    open fun flip(): PageLayout {
        return this
    }

    abstract fun initScrollArea()

    private var cachedScaledScrollAreaScale: Float? = null
    private val cachedScaledScrollArea = Rectangle()

    fun getScaledScrollArea(viewContext: ViewContext): Rectangle {
        val scale = viewContext.currentScale

        if (cachedScaledScrollAreaScale == scale) {
            return cachedScaledScrollArea
        }

        return calcScrollArea(viewContext, cachedScaledScrollArea).also {
            cachedScaledScrollAreaScale = scale
        }
    }

    fun calcScrollArea(viewContext: ViewContext, result: Rectangle): Rectangle {
        val marginHorizontalHalf = max(
            (viewContext.viewport.width - scrollArea.width),
            0.0F
        ) / 2
        val marginVerticalHalf = max(
            (viewContext.viewport.height - scrollArea.height),
            0.0F
        ) / 2

        result.copyFrom(scrollArea).also {
            it.left -= marginHorizontalHalf
            it.right += marginHorizontalHalf
            it.top -= marginVerticalHalf
            it.bottom += marginVerticalHalf
        }

        return result
    }

    fun containsPage(pageIndex: Int) = pages.any { it.index == pageIndex }
}
