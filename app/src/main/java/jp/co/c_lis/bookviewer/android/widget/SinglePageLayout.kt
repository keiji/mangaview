package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Log
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

        Log.d(TAG, "singlepage", page.position)
    }

    override val pages: List<Page>
        get() {
            val pageSnapshot = page ?: return emptyList()
            return listOf(pageSnapshot)
        }
}
