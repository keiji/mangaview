package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import dev.keiji.mangaview.Rectangle
import kotlin.math.min

enum class ContentState {
    NotReady,
    Preparing,
    Ready,
    Initializing,
    Initialized,
}

abstract class ContentLayer {

    companion object {
        private val TAG = ContentLayer::class.java.simpleName
    }

    var page: Page? = null

    abstract val contentWidth: Float
    abstract val contentHeight: Float

    internal var baseScale: Float = 1.0F

    var paddingLeft = 0.0F
    var paddingTop = 0.0F
    var paddingRight = 0.0F
    var paddingBottom = 0.0F

    private val globalRect = Rectangle()
    private val contentSrc = Rectangle()

    private val contentViewport = RectF()
    private val prevContentViewport = RectF()

    abstract fun onPrepareContent(viewContext: ViewContext, page: Page): Boolean

    open val isContentPrepared
        get() = false

    @Volatile
    private var state = ContentState.NotReady

    private fun init(page: Page) {
        state = ContentState.Initializing

        baseScale = min(
            page.globalRect.width / contentWidth,
            page.globalRect.height / contentHeight
        )

        val scaledContentWidth = contentWidth * baseScale
        val scaledContentHeight = contentHeight * baseScale

        val paddingHorizontal = (page.globalRect.width - scaledContentWidth)
        val paddingVertical = (page.globalRect.height - scaledContentHeight)

        paddingLeft = when (page.horizontalAlign) {
            PageHorizontalAlign.Center -> paddingHorizontal / 2
            PageHorizontalAlign.Left -> 0.0F
            PageHorizontalAlign.Right -> paddingHorizontal
        }
        paddingTop = when (page.verticalAlign) {
            PageVerticalAlign.Middle -> paddingVertical / 2
            PageVerticalAlign.Top -> 0.0F
            PageVerticalAlign.Bottom -> paddingVertical
        }
        paddingRight = paddingHorizontal - paddingLeft
        paddingBottom = paddingVertical - paddingTop

        globalRect.copyFrom(page.globalRect).also {
            it.left += paddingLeft
            it.top += paddingTop
            it.right -= paddingRight
            it.bottom -= paddingBottom
        }

        paddingLeft /= baseScale
        paddingTop /= baseScale
        paddingRight /= baseScale
        paddingBottom /= baseScale

        state = ContentState.Initialized
    }

    private val srcRect = Rect()
    private val dstRect = RectF()

    fun draw(
        canvas: Canvas?,
        viewContext: ViewContext,
        page: Page,
        paint: Paint,
        onContentViewportChangeListener: (ContentLayer, RectF) -> Unit
    ): Boolean {
        if (state == ContentState.NotReady || state == ContentState.Preparing) {
            state = ContentState.Preparing

            if (!isContentPrepared && !onPrepareContent(viewContext, page)) {
                return false
            }

            state = ContentState.Ready
        }

        init(page)

        if (page.displayProjection.area == 0.0F) {
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

                it.offset(-paddingLeft, -paddingTop)
            }

        contentViewport.also {
            it.left = contentSrc.left + paddingLeft
            it.top = contentSrc.top + paddingTop
            it.right = contentSrc.right - paddingRight
            it.bottom = contentSrc.bottom - paddingBottom
        }

        if (contentViewport != prevContentViewport) {
            onContentViewportChangeListener(this, contentViewport)
            prevContentViewport.set(contentViewport)
        }

        contentSrc.copyTo(srcRect)
        page.displayProjection
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

    private val localPointTmp = Rectangle()

    fun requestHandleEvent(
        globalX: Float,
        globalY: Float,
        onTapListener: OnTapListener
    ): Boolean {
        localPointTmp.set(globalX, globalY, globalX, globalY)

        if (!globalRect.contains(localPointTmp)) {
            return false
        }

        val localPoint = convertToLocal()
        if (localPoint.right > contentWidth || localPoint.bottom > contentHeight) {
            return false
        }

        return onTapListener.onTap(this, localPoint.centerX, localPoint.centerY)
    }

    fun requestHandleEvent(
        globalX: Float,
        globalY: Float,
        onDoubleTapListener: OnDoubleTapListener
    ): Boolean {
        localPointTmp.set(globalX, globalY, globalX, globalY)

        if (!globalRect.contains(localPointTmp)) {
            return false
        }

        val localPoint = convertToLocal()
        if (localPoint.right > contentWidth || localPoint.bottom > contentHeight) {
            return false
        }

        return onDoubleTapListener.onDoubleTap(this, localPoint.centerX, localPoint.centerY)
    }

    private fun convertToLocal(): Rectangle {
        return localPointTmp
            .relativeBy(globalRect).also {
                it.left /= baseScale
                it.top /= baseScale
                it.right /= baseScale
                it.bottom /= baseScale
            }
    }

    fun recycle() {
        onRecycle()

        state = ContentState.NotReady
    }

    open fun onRecycle() {
    }
}
