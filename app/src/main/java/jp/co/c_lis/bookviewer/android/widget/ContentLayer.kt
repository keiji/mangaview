package jp.co.c_lis.bookviewer.android.widget

import android.graphics.Canvas
import android.graphics.Paint
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.min

enum class PageHorizontalAlign {
    Center,
    Left,
    Right
}

enum class PageVerticalAlign {
    Middle,
    Top,
    Bottom
}

abstract class ContentLayer {

    private var alignHorizontal = PageHorizontalAlign.Center
    private var alignVertical = PageVerticalAlign.Middle

    abstract val contentWidth: Float
    abstract val contentHeight: Float

    internal var minScale: Float = 1.0F

    val contentPosition = Rectangle()
    val contentSrc = Rectangle()
    val contentDst = Rectangle()

    abstract suspend fun prepareContent(viewState: ViewState, pageRect: Rectangle)

    open val isPrepared
        get() = false

    private suspend fun prepare(viewState: ViewState, pageRect: Rectangle) {
        prepareContent(viewState, pageRect)

        minScale = min(
            viewState.viewWidth / contentWidth,
            viewState.viewHeight / contentHeight
        )

        val scaledContentWidth = contentWidth * minScale
        val scaledContentHeight = contentHeight * minScale

        val paddingHorizontal = pageRect.width - (scaledContentWidth / viewState.viewWidth)
        val paddingVertical = pageRect.height - (scaledContentHeight / viewState.viewHeight)

        val paddingLeft = paddingHorizontal / 2
        val paddingRight = paddingHorizontal - paddingLeft
        val paddingTop = paddingVertical / 2
        val paddingBottom = paddingVertical - paddingTop

        contentPosition.also {
            it.left = paddingLeft
            it.top = paddingTop
            it.right = 1.0F - paddingRight
            it.bottom = 1.0F - paddingBottom
        }
    }

    fun draw(
        canvas: Canvas?,
        viewState: ViewState,
        page: Page,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {
        if (!isPrepared) {
            coroutineScope.launch(Dispatchers.IO) {
                prepare(viewState, page.position)
            }
            return false
        }

        contentSrc.set(page.pageViewport)
        contentDst.set(page.destOnView)
        Rectangle.and(contentDst, contentPosition, contentDst)

        return onDraw(canvas, viewState, page, paint, coroutineScope)
    }

    abstract fun onDraw(
        canvas: Canvas?,
        viewState: ViewState,
        page: Page,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean
}
