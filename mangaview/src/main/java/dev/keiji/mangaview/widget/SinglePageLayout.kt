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

    override fun add(page: Page) {
        page.baseScale = min(
            position.width / page.width,
            position.height / page.height
        )

        val paddingHorizontal = position.width - page.scaledWidth
        val paddingVertical = position.height - page.scaledHeight

        val paddingLeft = paddingHorizontal / 2
        val paddingRight = paddingHorizontal - paddingLeft
        val paddingTop = paddingVertical / 2
        val paddingBottom = paddingVertical - paddingTop

        page.position.also {
            it.left = position.left + paddingLeft
            it.top = position.top + paddingTop
            it.right = position.right - paddingRight
            it.bottom = position.bottom - paddingBottom
        }

        this.page = page
        initScrollArea()

        Log.d(TAG, "singlepage", page.position)
    }

    override fun initScrollArea() {
        val pageSnapshot = page ?: return
        val pagePosition = pageSnapshot.position

        scrollArea.set(
            pagePosition.left,
            pagePosition.top,
            pagePosition.right,
            pagePosition.bottom
        )
        Log.d(TAG, "page:${pageSnapshot.index}", scrollArea)
    }

    override fun calcScrollArea(rectangle: Rectangle, viewContext: ViewContext): Rectangle {
        val scale = viewContext.currentScale
        val scaledScrollWidth = scrollArea.width * scale
        val scaledScrollHeight = scrollArea.height * scale

        val marginHorizontal = max(scaledScrollWidth - scrollArea.width, 0.0F)
        val marginVertical = max(scaledScrollHeight - scrollArea.height, 0.0F)

        rectangle.set(scrollArea).also {
            it.left -= marginHorizontal / 2
            it.right += marginHorizontal / 2
            it.top -= marginVertical / 2
            it.bottom += marginVertical / 2
        }

        return rectangle
    }

    override val pages: List<Page>
        get() {
            val pageSnapshot = page ?: return emptyList()
            return listOf(pageSnapshot)
        }
}
