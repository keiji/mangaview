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

abstract class ContentLayer {

    private var alignHorizontal = PageHorizontalAlign.Center
    private var alignVertical = PageVerticalAlign.Middle

    abstract val contentWidth: Float
    abstract val contentHeight: Float

    internal var minScale: Float = 1.0F

    var paddingLeft = 0
    var paddingTop = 0
    var paddingRight = 0
    var paddingBottom = 0

    val contentSrc = Rectangle()
    val destOnView = Rectangle()

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

        val paddingHorizontal = pageRect.width - scaledContentWidth
        val paddingVertical = pageRect.height - scaledContentHeight

        paddingLeft = ((paddingHorizontal / 2) / minScale).roundToInt()
        paddingTop = ((paddingVertical / 2) / minScale).roundToInt()
        paddingRight = (paddingHorizontal / minScale - paddingLeft).roundToInt()
        paddingBottom = (paddingVertical / minScale - paddingTop).roundToInt()
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
