package jp.co.c_lis.bookviewer.android.widget

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.annotation.VisibleForTesting
import jp.co.c_lis.bookviewer.android.Rectangle

data class ViewState(
    internal var viewWidth: Float = 0.0F,
    internal var viewHeight: Float = 0.0F,
    internal var scrollX: Float = 0.0F,
    internal var scrollY: Float = 0.0F,
    internal var currentScale: Float = 1.0F,
    internal val viewport: Rectangle = Rectangle(0.0F, 0.0F, viewWidth, viewHeight),
    internal val scrollableArea: Rectangle = Rectangle(0.0F, 0.0F, 1.0F, 1.0F)
) : GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {

    companion object {
        private val TAG = ViewState::class.java.simpleName
    }

    var minScale = 1.0F
    var maxScale = 5.0F

    val width: Float
        get() = viewWidth / currentScale

    val height: Float
        get() = viewHeight / currentScale

    override fun onShowPress(e: MotionEvent?) {
        Log.d(TAG, "onShowPress")
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.d(TAG, "onSingleTapUp")
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        Log.d(TAG, "onDown")
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d(TAG, "onFling")
        return false
    }

    private fun validate(): Boolean {
        var result = true

        viewport.set(
            scrollX,
            scrollY,
            scrollX + width,
            scrollY + height
        )

        if (viewport.left < scrollableArea.left) {
            offset(scrollableArea.left - viewport.left, 0.0F)
            result = false
        } else if (viewport.right > scrollableArea.right) {
            offset(scrollableArea.right - viewport.right, 0.0F)
            result = false
        }
        if (viewport.top < scrollableArea.top) {
            offset(0.0F, scrollableArea.top - viewport.top)
            result = false
        } else if (viewport.bottom > scrollableArea.bottom) {
            offset(0.0F, scrollableArea.bottom - viewport.bottom)
            result = false
        }

        return result
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean = offset(
        distanceX / currentScale,
        distanceY / currentScale
    )

    fun offset(
        offsetX: Float,
        offsetY: Float
    ): Boolean {
        scrollX += offsetX
        scrollY += offsetY

        return validate()
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.d(TAG, "onLongPress")
    }

    private var isScaling = false

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        Log.d(TAG, "onScaleBegin")

        detector ?: return false

        isScaling = true
        return true
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        detector ?: return false

        val factor = detector.scaleFactor

        val scale = currentScale * factor

        val result = setScale(
            scale,
            detector.focusX,
            detector.focusY
        )

        return result
    }

    @VisibleForTesting
    fun setScale(
        scale: Float,
        focusX: Float,
        focusY: Float
    ): Boolean {
        var newScale = scale

        if (maxScale < newScale) {
            newScale = maxScale
        }
        if (minScale > newScale) {
            newScale = minScale
        }
        if (currentScale == newScale) {
            return false
        }

        val focusXRatio = focusX / viewWidth
        val focusYRatio = focusY / viewHeight

        val newViewportWidth = viewWidth / newScale
        val newViewportHeight = viewHeight / newScale

        val diffX = newViewportWidth - width
        val diffY = newViewportHeight - height

        scrollX -= diffX * focusXRatio
        scrollY -= diffY * focusYRatio

        currentScale = newScale

        return validate()
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        Log.d(TAG, "onScaleEnd")

        isScaling = false
    }
}
