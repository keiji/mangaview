package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle
import kotlin.math.max
import kotlin.math.min

class SinglePageLayout : PageLayout() {

    companion object {
        private val TAG = SinglePageLayout::class.java.simpleName
    }

    override val isFilled: Boolean
        get() = page != null

    var page: Page? = null

    override val keyPage: Page?
        get() = page

    override fun add(page: Page) {
        page.baseScale = min(
            globalPosition.width / page.width,
            globalPosition.height / page.height
        )

        val paddingHorizontal = globalPosition.width - page.scaledWidth
        val paddingVertical = globalPosition.height - page.scaledHeight

        val paddingLeft = paddingHorizontal / 2
        val paddingRight = paddingHorizontal - paddingLeft
        val paddingTop = paddingVertical / 2
        val paddingBottom = paddingVertical - paddingTop

        page.globalRect.also {
            it.left = globalPosition.left + paddingLeft
            it.top = globalPosition.top + paddingTop
            it.right = globalPosition.right - paddingRight
            it.bottom = globalPosition.bottom - paddingBottom
        }

        this.page = page
        initScrollArea()

        Log.d(TAG, "singlepage", page.globalRect)
    }

    override fun replace(targetPage: Page, newPage: Page?) {
        if (page == targetPage) {
            page = newPage
        }
    }

    override fun initScrollArea() {
        val pageSnapshot = page ?: return
        val pagePosition = pageSnapshot.globalRect

        scrollArea.set(
            pagePosition.left,
            pagePosition.top,
            pagePosition.right,
            pagePosition.bottom
        )
        Log.d(TAG, "page:${pageSnapshot.index}", scrollArea)
    }

    override fun calcScrollArea(viewContext: ViewContext, result: Rectangle): Rectangle {
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

    override val pages: List<Page>
        get() {
            val pageSnapshot = page ?: return emptyList()
            return listOf(pageSnapshot)
        }
}
