package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.min

abstract class ContentLayer {

    companion object {
        private val TAG = ContentLayer::class.java.simpleName
    }

    var page: Page? = null

    abstract val contentWidth: Float
    abstract val contentHeight: Float

    internal var baseScale: Float = 1.0F

    var offsetX = 0.0F
    var offsetY = 0.0F

    private val globalRect = Rectangle()
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
            page.globalRect.width / contentWidth,
            page.globalRect.height / contentHeight
        )

        val scaledContentWidth = contentWidth * baseScale
        val scaledContentHeight = contentHeight * baseScale

        val paddingHorizontal = (page.globalRect.width - scaledContentWidth) / baseScale
        val paddingVertical = (page.globalRect.height - scaledContentHeight) / baseScale

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

        globalRect.copyFrom(page.globalRect).also {
            it.left += offsetX
            it.top += offsetY
            it.right -= offsetX
            it.bottom -= offsetY
        }

        Log.d(TAG, "offsetX, $offsetX")
        Log.d(TAG, "offsetY, $offsetY")
        Log.d(TAG, "baseScale $baseScale")
        Log.d(TAG, "globalRect", globalRect)

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

                it.offset(-offsetX, -offsetY)
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

    open fun recycle() {
    }
}
