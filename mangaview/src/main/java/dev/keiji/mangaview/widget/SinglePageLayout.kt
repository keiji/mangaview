package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle
import kotlin.math.min

class SinglePageLayout(index: Int) : PageLayout(index) {

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

        // Rounding padding values
        val padding = Rectangle(paddingLeft, paddingTop, paddingRight, paddingBottom)
            .apply { round() }

        page.globalRect.also {
            it.left = globalPosition.left + padding.left
            it.top = globalPosition.top + padding.top
            it.right = globalPosition.right - padding.right
            it.bottom = globalPosition.bottom - padding.bottom
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

    override val pages: List<Page>
        get() {
            val pageSnapshot = page ?: return emptyList()
            return listOf(pageSnapshot)
        }
}
