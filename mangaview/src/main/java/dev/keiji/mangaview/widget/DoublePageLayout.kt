package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Rectangle
import kotlin.math.max
import kotlin.math.min

class DoublePageLayout(
    index: Int,
    private val isSpread: Boolean,
    private val startOneSide: Boolean
) : PageLayout(index) {

    companion object {
        private val TAG = DoublePageLayout::class.java.simpleName
    }

    override val isFilled: Boolean
        get() = centerPage != null || (leftPage != null && rightPage != null)

    private var centerPage: Page? = null

    private var leftPage: Page? = null

    private var rightPage: Page? = null

    override val keyPage: Page?
        get() {
            if (centerPage != null) {
                return centerPage
            }

            return if (isFlip) {
                leftPage
            } else {
                rightPage
            }
        }

    override fun add(page: Page) {
        val centerPageSnapshot = centerPage
        val leftPageSnapshot = leftPage
        val rightPageSnapshot = rightPage

        if (centerPageSnapshot == null && leftPageSnapshot == null && rightPageSnapshot == null) {
            setCenterPage(page, globalPosition.width)
        } else if (centerPageSnapshot != null) {
            centerPage = null
            setLeftOrRightPage(centerPageSnapshot)
            setLeftOrRightPage(page)
        } else {
            setLeftOrRightPage(page)
        }

        initScrollArea()
    }

    fun setLeftOrRightPage(page: Page) {
        val layoutWidth = globalPosition.width / 2

        val value = page.index + if (isFlip) {
            1
        } else {
            0
        }

        if (value % 2 == 0) {
            setRightPage(page, layoutWidth)
        } else {
            setLeftPage(page, layoutWidth)
        }
    }

    override fun replace(targetPage: Page, newPage: Page?) {
        when {
            centerPage == targetPage -> {
                centerPage = newPage
            }
            leftPage == targetPage -> {
                leftPage = newPage
            }
            rightPage == targetPage -> {
                rightPage = newPage
            }
        }
    }

    override fun initScrollArea() {
        centerPage?.also {
            scrollArea.copyFrom(it.globalRect)
            return
        }

        val evenPagePosition = rightPage?.globalRect ?: return
        val oddPagePosition = leftPage?.globalRect ?: return

        scrollArea.set(
            min(evenPagePosition.left, oddPagePosition.left),
            min(evenPagePosition.top, oddPagePosition.top),
            max(evenPagePosition.right, oddPagePosition.right),
            max(evenPagePosition.bottom, oddPagePosition.bottom)
        )
    }

    private fun setCenterPage(page: Page, pageWidth: Float) {
        page.baseScale = min(
            pageWidth / page.width,
            globalPosition.height / page.height
        )

        val paddingHorizontal = pageWidth - page.scaledWidth
        val paddingVertical = globalPosition.height - page.scaledHeight

        val paddingLeft = paddingHorizontal / 2
        val paddingRight = paddingHorizontal - paddingLeft
        val paddingTop = paddingVertical / 2
        val paddingBottom = paddingVertical - paddingTop

        page.horizontalAlign = PageHorizontalAlign.Center

        page.globalRect.also {
            it.left = globalPosition.left + paddingLeft
            it.top = globalPosition.top + paddingTop
            it.right = globalPosition.right - paddingRight
            it.bottom = globalPosition.bottom - paddingBottom
        }

        centerPage = page
    }

    private fun setRightPage(page: Page, pageWidth: Float) {
        page.baseScale = min(
            pageWidth / page.width,
            globalPosition.height / page.height
        )

        val paddingHorizontal = pageWidth - page.scaledWidth
        val paddingVertical = globalPosition.height - page.scaledHeight

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

        page.globalRect.also {
            it.left = globalPosition.left + pageWidth + paddingLeft
            it.top = globalPosition.top + paddingTop
            it.right = globalPosition.right - paddingRight
            it.bottom = globalPosition.bottom - paddingBottom
        }

        rightPage = page
    }

    private fun setLeftPage(page: Page, pageWidth: Float) {
        page.baseScale = min(
            pageWidth / page.width,
            globalPosition.height / page.height
        )

        val paddingHorizontal = pageWidth - page.scaledWidth
        val paddingVertical = globalPosition.height - page.scaledHeight

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

        page.globalRect.also {
            it.left = globalPosition.left + paddingLeft
            it.top = globalPosition.top + paddingTop
            it.right = globalPosition.right - pageWidth - paddingRight
            it.bottom = globalPosition.bottom - paddingBottom
        }

        leftPage = page
    }

    override val pages: List<Page>
        get() {
            return if (!isFlip) {
                listOf(centerPage, leftPage, rightPage).filterNotNull()
            } else {
                listOf(centerPage, rightPage, leftPage).filterNotNull()
            }
        }

    private var isFlip = false

    override fun flip(): PageLayout {
        isFlip = !isFlip

        val evenPageSnapshot = rightPage ?: return this
        val oddPageSnapshot = leftPage ?: return this

        val tmp = Rectangle()

        tmp.copyFrom(oddPageSnapshot.globalRect)
        oddPageSnapshot.globalRect.copyFrom(evenPageSnapshot.globalRect)
        evenPageSnapshot.globalRect.copyFrom(tmp)

        if (isSpread) {
            oddPageSnapshot.horizontalAlign = PageHorizontalAlign.Left
            evenPageSnapshot.horizontalAlign = PageHorizontalAlign.Right
        }

        return this
    }

}
