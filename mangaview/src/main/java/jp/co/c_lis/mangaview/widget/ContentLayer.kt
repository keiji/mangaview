package jp.co.c_lis.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import jp.co.c_lis.mangaview.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

abstract class ContentLayer{

    companion object {
        private val TAG = ContentLayer::class.java.simpleName
    }

    abstract val contentWidth: Float
    abstract val contentHeight: Float

    internal var baseScale: Float = 1.0F

    var offsetX = 0.0F
    var offsetY = 0.0F

    private val contentSrc = Rectangle()
    private val projection = Rectangle()

    abstract suspend fun prepareContent(viewState: ViewState, page: Page)

    open val isPrepared
        get() = false

    internal var isPreparing = false

    private suspend fun prepare(viewState: ViewState, page: Page) {
        isPreparing = true

        prepareContent(viewState, page)

        baseScale = min(
            page.position.width / contentWidth,
            page.position.height / contentHeight
        )

        val scaledContentWidth = contentWidth * baseScale
        val scaledContentHeight = contentHeight * baseScale

        val paddingHorizontal = (page.position.width - scaledContentWidth) / baseScale
        val paddingVertical = (page.position.height - scaledContentHeight) / baseScale

        offsetX = when (page.horizontalAlign) {
            PageHorizontalAlign.Center -> paddingHorizontal / 2
            PageHorizontalAlign.Left -> 0.0F
            PageHorizontalAlign.Right -> paddingHorizontal
        }
        offsetY = when (page.verticalAlign) {
            PageVerticalAlign.Middle -> paddingVertical / 2
            PageVerticalAlign.Top -> 0.0F
            PageVerticalAlign.Bottom -> paddingVertical
        }

        isPreparing = false
    }

    private val srcRect = Rect()
    private val dstRect = RectF()

    fun draw(
        canvas: Canvas?,
        viewState: ViewState,
        page: Page,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {
        if (!isPrepared && !isPreparing) {
            coroutineScope.launch(Dispatchers.IO) {
                prepare(viewState, page)
            }
            return false
        }

        contentSrc.set(page.contentSrc).also {
            it.left = it.left / baseScale
            it.top = it.top / baseScale
            it.right = it.right / baseScale
            it.bottom = it.bottom / baseScale

            it.offset(-offsetX, -offsetY)
        }

        contentSrc.copyTo(srcRect)
        projection.set(page.projection)
            .copyTo(dstRect)

        return onDraw(canvas, srcRect, dstRect, viewState, paint, coroutineScope)
    }

    abstract fun onDraw(
        canvas: Canvas?,
        srcRect: Rect,
        dstRect: RectF,
        viewState: ViewState,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean

    open fun recycle() {
    }
}
