package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.VisibleForTesting
import dev.keiji.mangaview.Rectangle
import kotlin.math.min

abstract class ContentLayer(
    private val contentSource: ContentSource
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
    val globalPosition = Rectangle()

    @VisibleForTesting
    val contentSrc = Rectangle()

    private val contentViewport = RectF()

    private val prevContentViewport = RectF()

    private var state = State.NA

    private val tmpLocalPoint = Rectangle()

    private val contentSourcePrepareCallback = fun() {
        val pageSnapshot = page ?: return

        state = State.Initializing

        baseScale = min(
            pageSnapshot.globalRect.width / contentSource.contentWidth,
            pageSnapshot.globalRect.height / contentSource.contentHeight
        )

        val scaledContentWidth = contentSource.contentWidth * baseScale
        val scaledContentHeight = contentSource.contentHeight * baseScale

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

        globalPosition.copyFrom(pageSnapshot.globalRect).also {
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
        if (!contentSource.prepare(viewContext, contentSourcePrepareCallback)) {
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

        if (page.displayProjection.area == 0.0F) {
            // do not draw
            return true
        }

        return onDraw(canvas, page, viewContext, paint)
    }

    abstract fun onDraw(
        canvas: Canvas?,
        page: Page,
        viewContext: ViewContext,
        paint: Paint,
    ): Boolean

    fun requestHandleEvent(
        globalX: Float,
        globalY: Float,
        onTapListener: OnTapListener? = null
    ): Boolean {
        tmpLocalPoint.set(globalX, globalY, globalX, globalY)

        if (!globalPosition.contains(tmpLocalPoint)) {
            return false
        }

        val localPoint = convertToLocal()
        if (localPoint.right > contentSource.contentWidth || localPoint.bottom > contentSource.contentHeight) {
            return false
        }

        if (onTapListener == null) {
            onTap(localPoint.centerX, localPoint.centerY)
        }

        return onTapListener?.onTap(this, localPoint.centerX, localPoint.centerY) ?: false
    }

    open fun onTap(x: Float, y: Float) {
        // Do nothing
    }

    fun requestHandleEvent(
        globalX: Float,
        globalY: Float,
        onDoubleTapListenerList: List<OnDoubleTapListener>
    ): Boolean {
        tmpLocalPoint.set(globalX, globalY, globalX, globalY)

        if (!globalPosition.contains(tmpLocalPoint)) {
            return false
        }

        val localPoint = convertToLocal()
        if (localPoint.right > contentSource.contentWidth || localPoint.bottom > contentSource.contentHeight) {
            return false
        }

        var consumed = onDoubleTap(localPoint.centerX, localPoint.centerY)
        if (consumed) {
            return true
        }

        onDoubleTapListenerList.forEach {
            if (it.onDoubleTap(this, localPoint.centerX, localPoint.centerY)) {
                consumed = true
                return@forEach
            }
        }

        return consumed
    }

    open fun onDoubleTap(x: Float, y: Float): Boolean = false

    fun requestHandleEvent(
        mangaView: MangaView,
        globalX: Float,
        globalY: Float,
        onLongTapListener: OnLongTapListener? = null
    ): Boolean {
        tmpLocalPoint.set(globalX, globalY, globalX, globalY)


        if (!globalPosition.contains(tmpLocalPoint)) {
            return false
        }

        val localPoint = convertToLocal()
        if (localPoint.right > contentSource.contentWidth || localPoint.bottom > contentSource.contentHeight) {
            return false
        }

        if (onLongTapListener == null) {
            onLongTap(mangaView, localPoint.centerX, localPoint.centerY)
        }

        return onLongTapListener?.onLongTap(this, localPoint.centerX, localPoint.centerY) ?: false
    }

    open fun onLongTap(mangaView: MangaView, x: Float, y: Float) {
        // Do nothing
    }

    private fun convertToLocal(): Rectangle {
        return tmpLocalPoint
            .relativeBy(globalPosition).also {
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
