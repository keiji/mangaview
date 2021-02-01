package dev.keiji.mangaview.layer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.VisibleForTesting
import dev.keiji.mangaview.Rectangle
import dev.keiji.mangaview.source.ContentSource
import dev.keiji.mangaview.widget.MangaView
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.PageHorizontalAlign
import dev.keiji.mangaview.widget.PageVerticalAlign
import dev.keiji.mangaview.widget.ViewContext
import kotlin.math.min
import kotlin.math.round

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

    private val tmpLocalPosition = Rectangle()

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

        paddingLeft = round(paddingLeft)
        paddingTop = round(paddingTop)
        paddingRight = round(paddingRight)
        paddingBottom = round(paddingBottom)

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

    /**
     * Draw on the canvas.
     *
     * @param canvas The canvas on which to draw the layer
     * @param page The page on which to draw the layer
     * @param viewContext The ViewContext the MangaView is running in
     * @param paint The paint used to compose the layer
     *
     * @return `true` the layer is drawn,
     *         `false` otherwise
     */
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
                it.round()

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

    /**
     * Calls this layer's onDraw event.
     *
     * @param canvas The canvas on which to draw the layer
     * @param page The page on which to draw the layer
     * @param viewContext The ViewContext the MangaView is running in
     * @param paint The paint used to compose the layer
     *
     * @return `true` the layer is drawn,
     *         `false` otherwise
     */
    abstract fun onDraw(
        canvas: Canvas?,
        page: Page,
        viewContext: ViewContext,
        paint: Paint,
    ): Boolean

    /**
     * Request handling this layer's tap event.
     *
     * @param globalX `globalX` coordinate of the anchoring tap event
     * @param globalY `globalY` coordinate of the anchoring tap event
     * @param onTapListenerList List of onTapListener
     *
     * @return `true` if the event is consumed,
     *         `false` otherwise
     */
    fun requestHandleOnTapEvent(
        globalX: Float,
        globalY: Float,
        onTapListenerList: List<MangaView.OnTapListener>
    ): Boolean {
        tmpLocalPosition.set(globalX, globalY, globalX, globalY)

        if (!globalPosition.contains(tmpLocalPosition)) {
            return false
        }

        val localPoint = convertToLocalPosition()
        if (localPoint.right > contentSource.contentWidth || localPoint.bottom > contentSource.contentHeight) {
            return false
        }

        var consumed = onTap(localPoint.centerX, localPoint.centerY)
        if (consumed) {
            return true
        }

        onTapListenerList.forEach {
            if (it.onTap(this, localPoint.centerX, localPoint.centerY)) {
                consumed = true
                return@forEach
            }
        }

        return consumed
    }

    /**
     * Calls this layer's onTap event, if it is override.
     * Invokes the onTap event did not consume the event, anchoring it to an (x,y) coordinate.
     *
     * @param x `x` coordinate of the anchoring tap event
     * @param y `y` coordinate of the anchoring tap event
     *
     * @return `true` if the event is consumed,
     *         `false` otherwise
     */
    open fun onTap(x: Float, y: Float): Boolean = false

    /**
     * Request handling this layer's double tap event.
     *
     * @param globalX `globalX` coordinate of the anchoring double tap event
     * @param globalY `globalY` coordinate of the anchoring double tap event
     * @param onDoubleTapListenerList List of onDoubleTapListener
     *
     * @return `true` if the event is consumed,
     *         `false` otherwise
     */
    fun requestHandleOnDoubleTapEvent(
        globalX: Float,
        globalY: Float,
        onDoubleTapListenerList: List<MangaView.OnDoubleTapListener>
    ): Boolean {
        tmpLocalPosition.set(globalX, globalY, globalX, globalY)

        if (!globalPosition.contains(tmpLocalPosition)) {
            return false
        }

        val localPoint = convertToLocalPosition()
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

    /**
     * Calls this layer's onDoubleTap event, if it is override.
     * Invokes the onDoubleTap event did not consume the event, anchoring it to an (x,y) coordinate.
     *
     * @param x `x` coordinate of the anchoring double tap event
     * @param y `y` coordinate of the anchoring double tap event
     *
     * @return `true` if the event is consumed,
     *         `false` otherwise
     */
    open fun onDoubleTap(x: Float, y: Float): Boolean = false

    /**
     * Request handling this layer's onLongTap event.
     *
     * @param globalX `globalX` coordinate of the anchoring long tap event
     * @param globalY `globalY` coordinate of the anchoring long tap event
     * @param onLongTapListenerList List of onLongTapListener
     *
     * @return `true` if the event is consumed,
     *         `false` otherwise
     */
    fun requestHandleOnLongTapEvent(
        globalX: Float,
        globalY: Float,
        onLongTapListenerList: List<MangaView.OnLongTapListener>
    ): Boolean {
        tmpLocalPosition.set(globalX, globalY, globalX, globalY)


        if (!globalPosition.contains(tmpLocalPosition)) {
            return false
        }

        val localPosition = convertToLocalPosition()
        if (localPosition.right > contentSource.contentWidth || localPosition.bottom > contentSource.contentHeight) {
            return false
        }

        var consumed = onLongTap(localPosition.centerX, localPosition.centerY)

        onLongTapListenerList.forEach {
            if (it.onLongTap(this, localPosition.centerX, localPosition.centerY)) {
                consumed = true
                return@forEach
            }
        }

        return consumed
    }

    /**
     * Calls this layer's onLongTap event, if it is override.
     * Invokes the onLongTap event did not consume the event, anchoring it to an (x,y) coordinate.
     *
     * @param x `x` coordinate of the anchoring long tap event
     * @param y `y` coordinate of the anchoring long tap event
     *
     * @return `true` if the event is consumed,
     *         `false` otherwise
     */
    open fun onLongTap(x: Float, y: Float): Boolean = false

    private fun convertToLocalPosition(): Rectangle {
        return tmpLocalPosition
            .relativeBy(globalPosition).also {
                it.left /= baseScale
                it.top /= baseScale
                it.right /= baseScale
                it.bottom /= baseScale
            }
    }

    /**
     * Free objects associated with this layer.
     */
    fun recycle() {
        onRecycled()

        state = State.NA
    }

    /**
     * Calls this layer's onRecycled event.
     */
    open fun onRecycled() {
    }
}
