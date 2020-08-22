package jp.co.c_lis.mangaview.widget

import jp.co.c_lis.mangaview.Log
import jp.co.c_lis.mangaview.Rectangle
import kotlin.math.max
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

        val value = page.index + if (isFlip) {
            1
        } else {
            0
        }

        if (value % 2 == 0) {
            // even
            addEvenPage(page, layoutWidth)
        } else {
            // odd
            addOddPage(page, layoutWidth)
        }

        initScrollArea()
    }

    override fun initScrollArea() {
        val evenPagePosition = evenPage?.position ?: return
        val oddPagePosition = oddPage?.position ?: return

        scrollArea.set(
            min(evenPagePosition.left, oddPagePosition.left),
            min(evenPagePosition.top, oddPagePosition.top),
            max(evenPagePosition.right, oddPagePosition.right),
            max(evenPagePosition.bottom, oddPagePosition.bottom)
        )
    }


    private fun addEvenPage(page: Page, pageWidth: Float) {
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
            page.horizontalAlign = PageHorizontalAlign.Left
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

    private fun addOddPage(page: Page, pageWidth: Float) {
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
            page.horizontalAlign = PageHorizontalAlign.Right
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

    override fun flip(): PageLayout {
        val evenPageSnapshot = evenPage ?: return this
        val oddPageSnapshot = oddPage ?: return this

        val tmp = Rectangle()

        tmp.set(oddPageSnapshot.position)
        oddPageSnapshot.position.set(evenPageSnapshot.position)
        evenPageSnapshot.position.set(tmp)

        if (isSpread) {
            oddPageSnapshot.horizontalAlign = PageHorizontalAlign.Left
            evenPageSnapshot.horizontalAlign = PageHorizontalAlign.Right
        }

        isFlip = !isFlip

        return this
    }

    override fun calcScrollArea(
        rectangle: Rectangle,
        viewContext: ViewContext
    ): Rectangle {
        val scale = viewContext.currentScale

        val scaledScrollWidth = scrollArea.width * scale
        val scaledScrollHeight = scrollArea.height * scale

        val marginHorizontal = max(position.width - scaledScrollWidth, 0.0F)
        val marginVertical = max(position.height - scaledScrollHeight, 0.0F)

        rectangle.set(scrollArea).also {
            it.left -= marginHorizontal / 2
            it.right += marginHorizontal - marginHorizontal / 2
            it.top -= marginVertical / 2
            it.bottom += marginVertical - marginVertical / 2
        }

        return rectangle
    }
}
