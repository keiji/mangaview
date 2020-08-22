package dev.keiji.mangaview

import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class Rectangle(
    var left: Float = 0.0F,
    var top: Float = 0.0F,
    var right: Float = 0.0F,
    var bottom: Float = 0.0F
) {

    val centerX: Float
        get() = left + width / 2

    val centerY: Float
        get() = top + height / 2

    val width: Float
        get() = right - left
    val height: Float
        get() = bottom - top

    val area: Float
        get() = width * height

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

    fun contains(rectangle: Rectangle): Boolean {
        if (left <= rectangle.left && right >= rectangle.right
            && top <= rectangle.top && bottom >= rectangle.bottom
        ) {
            return true
        }

        return false
    }

    fun relativeBy(base: Rectangle): Rectangle {
        offset(-base.left, -base.top)
        return this
    }

    fun and(rectB: Rectangle): Rectangle? {
        return and(this, rectB, this)
    }

    fun validate() {
        if (left > right) {
            val tmp = left
            left = right
            right = tmp
        }
        if (top > bottom) {
            val tmp = top
            top = bottom
            bottom = tmp
        }
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

        fun jaccardIndex(
            rectA: Rectangle,
            rectB: Rectangle,
            tmp: Rectangle = Rectangle()
        ): Float {
            if (!rectA.intersect(rectB)) {
                return 0.0F
            }

            val overlap = and(rectA, rectB)?.area ?: 0.0F
            val jaccardIndex = overlap / ((rectA.area + rectB.area) - overlap)

            return jaccardIndex
        }
    }

    fun equals(rect: Rectangle, delta: Float): Boolean {
        if (this === rect) {
            return true
        }
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

    fun copyTo(rect: Rect) {
        rect.left = left.roundToInt()
        rect.right = right.roundToInt()
        rect.top = top.roundToInt()
        rect.bottom = bottom.roundToInt()
    }

    fun copyTo(rect: RectF) {
        rect.left = left
        rect.right = right
        rect.top = top
        rect.bottom = bottom
    }
}
