package jp.co.c_lis.bookviewer.android.widget

import android.graphics.Canvas
import android.graphics.Paint
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

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

abstract class ContentLayer(
    private val alignHorizontal: PageHorizontalAlign = PageHorizontalAlign.Center,
    private val alignVertical: PageVerticalAlign = PageVerticalAlign.Middle
) {


    abstract val contentWidth: Float
    abstract val contentHeight: Float

    internal var minScale: Float = 1.0F

    var paddingLeft = 0
    var paddingTop = 0
    var paddingRight = 0
    var paddingBottom = 0

    val contentSrc = Rectangle()
    val destOnView = Rectangle()

    abstract suspend fun prepareContent(viewState: ViewState, page: Page)

    open val isPrepared
        get() = false

    private suspend fun prepare(viewState: ViewState, page: Page) {
        prepareContent(viewState, page)

        minScale = min(
            viewState.viewWidth / contentWidth,
            viewState.viewHeight / contentHeight
        )

        val scaledContentWidth = contentWidth * minScale
        val scaledContentHeight = contentHeight * minScale

        val paddingHorizontal = page.position.width - scaledContentWidth
        val paddingVertical = page.position.height - scaledContentHeight

        alignment(paddingHorizontal, paddingVertical)
    }

    private fun alignment(paddingHorizontal: Float, paddingVertical: Float) {
        when (alignHorizontal) {
            PageHorizontalAlign.Center -> {
                paddingLeft = ((paddingHorizontal / 2) / minScale).roundToInt()
                paddingRight = (paddingHorizontal / minScale - paddingLeft).roundToInt()
            }
            PageHorizontalAlign.Left -> {
                paddingLeft = 0
                paddingRight = (paddingHorizontal / minScale - paddingLeft).roundToInt()
            }
            PageHorizontalAlign.Right -> {
                paddingLeft = (paddingHorizontal / minScale).roundToInt()
                paddingRight = 0
            }
        }

        when (alignVertical) {
            PageVerticalAlign.Middle -> {
                paddingTop = ((paddingVertical / 2) / minScale).roundToInt()
                paddingBottom = (paddingVertical / minScale - paddingTop).roundToInt()
            }
            PageVerticalAlign.Top -> {
                paddingTop = 0
                paddingBottom = (paddingVertical / minScale).roundToInt()
            }
            PageVerticalAlign.Bottom -> {
                paddingTop = (paddingVertical / minScale).roundToInt()
                paddingBottom = 0
            }
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
                prepare(viewState, page)
            }
            return false
        }

        contentSrc.set(page.contentSrc)
        destOnView.set(page.destOnView)

        return onDraw(canvas, viewState, paint, coroutineScope)
    }

    abstract fun onDraw(
        canvas: Canvas?,
        viewState: ViewState,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean
}
