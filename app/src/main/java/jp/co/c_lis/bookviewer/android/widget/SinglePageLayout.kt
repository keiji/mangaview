package jp.co.c_lis.bookviewer.android.widget

import android.graphics.Rect
import jp.co.c_lis.bookviewer.android.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlin.math.abs
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
        setPopulateAreas(page)
        setScrollArea(page)

        Log.d(TAG, "singlepage", page.position)
    }

    private fun setScrollArea(page: Page) {
        val pagePosition = page.position
        scrollArea.set(
            pagePosition.left,
            pagePosition.top,
            pagePosition.right,
            pagePosition.bottom
        )
        Log.d(TAG, "page:${page.index}", scrollArea)
    }

    override fun calcScrollArea(rectangle: Rectangle, scale: Float): Rectangle {
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

    private fun setPopulateAreas(page: Page) {
        val pagePosition = page.position

        populateAreaLeft.set(
            position.left, position.top,
            pagePosition.left, position.bottom
        ).also {
            it.left -= pagePosition.width
        }
        populateAreaTop.set(
            position.left, position.top,
            position.right, pagePosition.top
        ).also {
            it.top -= pagePosition.height
        }
        populateAreaRight.set(
            pagePosition.right, position.top,
            position.right, position.bottom
        ).also {
            it.right += pagePosition.width
        }
        populateAreaBottom.set(
            position.left, pagePosition.bottom,
            position.right, position.bottom
        ).also {
            it.bottom += pagePosition.height
        }
    }

    override val pages: List<Page>
        get() {
            val pageSnapshot = page ?: return emptyList()
            return listOf(pageSnapshot)
        }
}
