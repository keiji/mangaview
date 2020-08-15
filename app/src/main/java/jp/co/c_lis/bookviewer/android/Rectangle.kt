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

    fun set(left: Float, top: Float, right: Float, bottom: Float): Rectangle {
        this.left = left
        this.right = right
        this.top = top
        this.bottom = bottom
        return this
    }

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

    fun relativeBy(base: Rectangle): Rectangle {
        offset(-base.left, -base.top)
        return this
    }

    fun and(rectB: Rectangle): Rectangle? {
        return and(this, rectB, this)
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
