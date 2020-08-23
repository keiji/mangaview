package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import dev.keiji.mangaview.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.min

abstract class ContentLayer {

    companion object {
        private val TAG = ContentLayer::class.java.simpleName
    }

    abstract val contentWidth: Float
    abstract val contentHeight: Float

    internal var baseScale: Float = 1.0F

    var offsetX = 0.0F
    var offsetY = 0.0F

    private val contentSrc = Rectangle()

    abstract fun prepareContent(viewContext: ViewContext, page: Page)

    open val isPrepared
        get() = false

    @Volatile
    private var isPreparing = false

    private val needPrepare: Boolean
        get() = !isPrepared && !isPreparing

    private fun prepare(viewContext: ViewContext, page: Page) {
        isPreparing = true

        prepareContent(viewContext, page)

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
        viewContext: ViewContext,
        page: Page,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {
        if (needPrepare) {
            coroutineScope.launch(Dispatchers.IO) {
                prepare(viewContext, page)
            }
            return false
        }

        if (page.projection.area == 0.0F) {
            // do not draw
            return true
        }

        contentSrc
            .copyFrom(page.contentSrc)
            .also {
                it.left = it.left / baseScale
                it.top = it.top / baseScale
                it.right = it.right / baseScale
                it.bottom = it.bottom / baseScale

                it.offset(-offsetX, -offsetY)
            }

        contentSrc.copyTo(srcRect)
        page.projection
            .copyTo(dstRect)

        return onDraw(canvas, srcRect, dstRect, viewContext, paint)
    }

    abstract fun onDraw(
        canvas: Canvas?,
        srcRect: Rect,
        dstRect: RectF,
        viewContext: ViewContext,
        paint: Paint,
    ): Boolean

    open fun recycle() {
    }
}
