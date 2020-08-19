package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlin.math.min

class DoublePageLayout(
    private val isSpread: Boolean
) : PageLayout() {

    companion object {
        private val TAG = DoublePageLayout::class.java.simpleName
    }

    override val isFilled: Boolean
        get() = (oddPage != null && evenPage != null)

    var oddPage: Page? = null
    var evenPage: Page? = null

    override fun add(page: Page) {
        val layoutWidth = position.width / 2

        if (page.index % 2 == 0) {
            // even
            addEvenPage(page, layoutWidth)
        } else {
            // odd
            addOddPage(page, layoutWidth)
        }
    }

    private fun addOddPage(page: Page, pageWidth: Float) {
        page.baseScale = min(
            pageWidth / page.width,
            position.height / page.height
        )

        val paddingHorizontal = pageWidth - page.scaledWidth
        val paddingVertical = position.height - page.scaledHeight

        var paddingLeft = 0.0F
        var paddingRight = 0.0F
        val paddingTop = paddingVertical / 2
        val paddingBottom = paddingVertical - paddingTop

        if (isSpread) {
            paddingRight = paddingHorizontal
        } else {
            paddingRight = paddingHorizontal / 3 * 2
            paddingLeft = paddingHorizontal - paddingRight
        }

        page.position.also {
            it.left = position.left + pageWidth + paddingLeft
            it.top = position.top + paddingTop
            it.right = position.right - paddingRight
            it.bottom = position.bottom - paddingBottom
        }

        evenPage = page
    }

    private fun addEvenPage(page: Page, pageWidth: Float) {
        page.baseScale = min(
            pageWidth / page.width,
            position.height / page.height
        )

        val paddingHorizontal = pageWidth - page.scaledWidth
        val paddingVertical = position.height - page.scaledHeight

        Log.d(TAG, "${paddingHorizontal}")

        var paddingLeft = pageWidth
        var paddingRight = 0.0F
        val paddingTop = paddingVertical / 2
        val paddingBottom = paddingVertical - paddingTop

        if (isSpread) {
            paddingLeft = paddingHorizontal
        } else {
            paddingLeft = paddingHorizontal / 3 * 2
            paddingRight = paddingHorizontal - paddingLeft
        }

        page.position.also {
            it.left = position.left + paddingLeft
            it.top = position.top + paddingTop
            it.right = position.right - pageWidth - paddingRight
            it.bottom = position.bottom - paddingBottom
        }

        oddPage = page
    }

    override val pages: List<Page>
        get() {
            val evenPageSnapshot = evenPage ?: return emptyList<Page>()
            val oddPageSnapshot = oddPage ?: return emptyList<Page>()

            return if (!isFlip) {
                listOf(oddPageSnapshot, evenPageSnapshot)
            } else {
                listOf(evenPageSnapshot, oddPageSnapshot)
            }
        }

    private var isFlip = false

    override fun flip() {
        val evenPageSnapshot = evenPage ?: return
        val oddPageSnapshot = oddPage ?: return

        val tmp = Rectangle()

        tmp.set(oddPageSnapshot.position)
        oddPageSnapshot.position.set(evenPageSnapshot.position)
        evenPageSnapshot.position.set(tmp)

        isFlip = !isFlip
    }
}
