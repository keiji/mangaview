package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Rectangle

class DoublePageLayout(
    private val isSpread: Boolean
) : PageLayout() {

    override val isFilled: Boolean
        get() = (oddPage != null && evenPage != null)

    var oddPage: Page? = null
    var evenPage: Page? = null

    override fun add(page: Page) {
        val layoutWidth = position.width / 2

        if (page.index % 2 == 0) {
            // even
            page.position.also {
                it.left = position.left
                it.right = position.left + layoutWidth
                it.top = position.top
                it.bottom = position.bottom
            }
            if (isSpread) {
                page.setAlignment(PageHorizontalAlign.Left)
            }
            evenPage = page

        } else {
            // odd
            page.position.also {
                it.left = position.left + layoutWidth
                it.right = position.right
                it.top = position.top
                it.bottom = position.bottom
            }
            if (isSpread) {
                page.setAlignment(PageHorizontalAlign.Right)
            }
            oddPage = page
        }
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

        if (isSpread) {
            when (isFlip) {
                true -> {
                    oddPageSnapshot.setAlignment(PageHorizontalAlign.Left)
                    evenPageSnapshot.setAlignment(PageHorizontalAlign.Right)
                }
                false -> {
                    oddPageSnapshot.setAlignment(PageHorizontalAlign.Right)
                    evenPageSnapshot.setAlignment(PageHorizontalAlign.Left)
                }
            }
        }

        isFlip = !isFlip
    }
}
