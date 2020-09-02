package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.VisibleForTesting
import dev.keiji.mangaview.Rectangle
import kotlin.math.min

abstract class ContentLayer(
    private val imageSource: ImageSource
) {

    companion object {
        private val TAG = ContentLayer::class.java.simpleName
    }

    enum class State {
        NA,
        Waiting,
        Initializing,
        Initialized,
    }

    var page: Page? = null

    internal var baseScale: Float = 1.0F

    var paddingLeft = 0.0F
    var paddingTop = 0.0F
    var paddingRight = 0.0F
    var paddingBottom = 0.0F

    @VisibleForTesting
    val globalRect = Rectangle()

    @VisibleForTesting
    val contentSrc = Rectangle()

    private val contentViewport = RectF()

    private val prevContentViewport = RectF()

    @VisibleForTesting
    val srcRect = Rect()

    @VisibleForTesting
    val dstRect = RectF()

    private var state = State.NA

    private val onImageSourceLoaded = fun() {
        val pageSnapshot = page ?: return

        state = State.Initializing

        baseScale = min(
            pageSnapshot.globalRect.width / imageSource.contentWidth,
            pageSnapshot.globalRect.height / imageSource.contentHeight
        )

        val scaledContentWidth = imageSource.contentWidth * baseScale
        val scaledContentHeight = imageSource.contentHeight * baseScale

        val paddingHorizontal = (pageSnapshot.globalRect.width - scaledContentWidth)
        val paddingVertical = (pageSnapshot.globalRect.height - scaledContentHeight)

        paddingLeft = when (pageSnapshot.horizontalAlign) {
            PageHorizontalAlign.Center -> paddingHorizontal / 2
            PageHorizontalAlign.Left -> 0.0F
            PageHorizontalAlign.Right -> paddingHorizontal
        }
        paddingTop = when (pageSnapshot.verticalAlign) {
            PageVerticalAlign.Middle -> paddingVertical / 2
            PageVerticalAlign.Top -> 0.0F
            PageVerticalAlign.Bottom -> paddingVertical
        }
        paddingRight = paddingHorizontal - paddingLeft
        paddingBottom = paddingVertical - paddingTop

        globalRect.copyFrom(pageSnapshot.globalRect).also {
            it.left += paddingLeft
            it.top += paddingTop
            it.right -= paddingRight
            it.bottom -= paddingBottom
        }

        paddingLeft /= baseScale
        paddingTop /= baseScale
        paddingRight /= baseScale
        paddingBottom /= baseScale

        state = State.Initialized
    }

    fun draw(
        canvas: Canvas?,
        viewContext: ViewContext,
        page: Page,
        paint: Paint,
        onContentViewportChangeListener: (ContentLayer, RectF) -> Unit
    ): Boolean {
        if (!imageSource.load(viewContext, onImageSourceLoaded)) {
            state = State.Waiting
            return false
        }

        if (state != State.Initialized) {
            return false
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

        if (page.displayProjection.area == 0.0F) {
            // do not draw
            return true
        }

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
        if (localPoint.right > imageSource.contentWidth || localPoint.bottom > imageSource.contentHeight) {
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
        if (localPoint.right > imageSource.contentWidth || localPoint.bottom > imageSource.contentHeight) {
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
        onRecycled()

        state = State.NA
    }

    open fun onRecycled() {
    }
}
