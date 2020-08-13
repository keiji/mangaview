package jp.co.c_lis.bookviewer.android

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Rectangle(
    var left: Float = 0.0F,
    var top: Float = 0.0F,
    var right: Float = 0.0F,
    var bottom: Float = 0.0F
) {

    val width: Float
        get() = right - left
    val height: Float
        get() = bottom - top

    fun set(rectangle: Rectangle): Rectangle {
        left = rectangle.left
        right = rectangle.right
        top = rectangle.top
        bottom = rectangle.bottom
        return this
    }

    fun offset(offsetX: Float, offsetY: Float) {
        left += offsetX
        right += offsetX
        top += offsetY
        bottom += offsetY
    }

    fun intersect(rectangle: Rectangle): Boolean {
        if (left > rectangle.right) {
            return false
        }
        if (right < rectangle.left) {
            return false
        }
        if (top > rectangle.bottom) {
            return false
        }
        if (bottom < rectangle.top) {
            return false
        }
        return true
    }

    fun relativeBy(base: Rectangle) {
        offset(-base.left, -base.top)
    }

    fun alignZero() {
        offset(-left, -top)
    }

    private fun scaleX(factor: Float, focusX: Float): Boolean {
        val focusXRatio = width * focusX

        val oldWidth = width
        val newWidth = width / factor
        if (newWidth <= 0.0F) {
            return false
        }

        val diffHorizontal = newWidth - oldWidth

        val diffLeft = diffHorizontal * focusXRatio
        val diffRight = diffHorizontal - diffLeft

        left -= diffLeft
        right += diffRight

        return true
    }

    private fun scaleY(factor: Float, focusY: Float): Boolean {
        val focusYRatio = height * focusY

        val oldHeight = height
        val newHeight = height / factor
        if (newHeight <= 0.0F) {
            return false
        }

        val diffVertical = newHeight - oldHeight

        val diffTop = diffVertical * focusYRatio
        val diffBottom = diffVertical - diffTop

        top -= diffTop
        bottom += diffBottom

        return true
    }

    fun scale(factor: Float, focusX: Float, focusY: Float): Boolean {
        return scaleX(factor, focusX) && scaleY(factor, focusY)
    }

    fun setScale(scale: Float, focusX: Float, focusY: Float): Boolean {
        return setScaleX(scale, focusX) && setScaleY(scale, focusY)
    }

    private fun setScaleX(scale: Float, focusX: Float): Boolean {
        val focusXRatio = width * focusX

        val oldWidth = width
        val newWidth = 1.0F / scale
        if (newWidth <= 0.0F) {
            return false
        }

        val diffHorizontal = newWidth - oldWidth

        val diffLeft = diffHorizontal * focusXRatio
        val diffRight = diffHorizontal - diffLeft

        left -= diffLeft
        right += diffRight

        return true

    }

    private fun setScaleY(scale: Float, focusY: Float): Boolean {
        val focusYRatio = height * focusY

        val oldHeight = height
        val newHeight = 1.0F / scale
        if (newHeight <= 0.0F) {
            return false
        }

        val diffVertical = newHeight - oldHeight

        val diffTop = diffVertical * focusYRatio
        val diffBottom = diffVertical - diffTop

        top -= diffTop
        bottom += diffBottom

        return true

    }

    companion object {
        private val TAG = Rectangle::class.java.simpleName

        private fun floatIsDifferent(
            f1: Float,
            f2: Float,
            delta: Float
        ): Boolean {
            if (f1.compareTo(f2) == 0) {
                return false
            }
            return abs(f1 - f2) > delta
        }

        fun or(
            rectA: Rectangle,
            rectB: Rectangle,
            result: Rectangle = Rectangle()
        ): Rectangle? {
            if (!rectA.intersect(rectB)) {
                return null
            }
            return result.also {
                it.left = min(rectA.left, rectB.left)
                it.right = max(rectA.right, rectB.right)
                it.top = min(rectA.top, rectB.top)
                it.bottom = max(rectA.bottom, rectB.bottom)
            }
        }

        fun and(
            rectA: Rectangle,
            rectB: Rectangle,
            result: Rectangle = Rectangle()
        ): Rectangle? {
            if (!rectA.intersect(rectB)) {
                return null
            }
            return result.also {
                it.left = max(rectA.left, rectB.left)
                it.right = min(rectA.right, rectB.right)
                it.top = max(rectA.top, rectB.top)
                it.bottom = min(rectA.bottom, rectB.bottom)
            }
        }
    }

    fun equals(rect: Rectangle, delta: Float): Boolean {
        if (floatIsDifferent(left, rect.left, delta)) {
            return false
        }
        if (floatIsDifferent(top, rect.top, delta)) {
            return false
        }
        if (floatIsDifferent(right, rect.right, delta)) {
            return false
        }
        if (floatIsDifferent(bottom, rect.bottom, delta)) {
            return false
        }
        return true
    }
}
