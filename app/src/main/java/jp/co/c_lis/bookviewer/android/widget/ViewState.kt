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
    internal val viewport: Rectangle = Rectangle(0.0F, 0.0F, 1.0F, 1.0F),
    internal val scrollableArea: Rectangle = Rectangle(0.0F, 0.0F, 1.0F, 1.0F)
) : GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {

    companion object {
        private val TAG = ViewState::class.java.simpleName
    }

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

    private fun validate(
        focusX: Float = 0.5F,
        focusY: Float = 0.5F
    ): Boolean {
        var result = true

        if (viewport.width > scrollableArea.width) {
            viewport.scale(
                1.0F / (scrollableArea.width / viewport.width),
                focusX, focusY
            )
            result = false
        }
        if (viewport.height > scrollableArea.height) {
            viewport.scale(
                1.0F / (scrollableArea.height / viewport.height),
                focusX, focusY
            )
            result = false
        }
        if (viewport.left < scrollableArea.left) {
            viewport.offset(scrollableArea.left - viewport.left, 0.0F)
            result = false
        } else if (viewport.right > scrollableArea.right) {
            viewport.offset(scrollableArea.right - viewport.right, 0.0F)
            result = false
        }
        if (viewport.top < scrollableArea.top) {
            viewport.offset(0.0F, scrollableArea.top - viewport.top)
            result = false
        } else if (viewport.bottom > scrollableArea.bottom) {
            viewport.offset(0.0F, scrollableArea.bottom - viewport.bottom)
            result = false
        }

        return result
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean = onScroll(distanceX / viewWidth, distanceY / viewHeight)

    fun onScroll(
        distanceRatioX: Float,
        distanceRatioY: Float
    ): Boolean {
        viewport.offset(distanceRatioX, distanceRatioY)
        return validate()
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.d(TAG, "onLongPress")
    }

    val scale: Float
        get() = 1 / ((viewport.width + viewport.height) / 2)

    private var isScaling = false
    var prevScale: Float = 1.0F

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        Log.d(TAG, "onScaleBegin")

        detector ?: return false

        isScaling = true
        prevScale = detector.scaleFactor
        return true
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        detector ?: return false

        val factor = detector.scaleFactor
        if (Math.abs(factor) < 0.0001) {
            return false
        }

        val result = onScale(
            factor,
            detector.focusX,
            detector.focusY
        )
        prevScale = factor

        return result
    }

    @VisibleForTesting
    fun onScale(
        factor: Float,
        focusX: Float,
        focusY: Float
    ): Boolean {
        val result = viewport.scale(
            factor,
            focusX / viewWidth,
            focusY / viewHeight
        )
        return validate(focusX, focusY) && result
    }

    @VisibleForTesting
    fun setScale(
        scale: Float,
        focusX: Float,
        focusY: Float
    ): Boolean {
        val result = viewport.setScale(
            scale,
            focusX / viewWidth,
            focusY / viewHeight
        )
        return validate(focusX, focusY) && result
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        Log.d(TAG, "onScaleEnd")

        prevScale = 1.0F
        isScaling = false
    }
}
