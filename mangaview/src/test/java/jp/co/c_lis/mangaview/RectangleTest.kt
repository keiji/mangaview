package jp.co.c_lis.mangaview

import org.junit.Assert.*
import org.junit.Test

class RectangleTest {

    @Test
    fun intersect_horizontal1() {
        val rect1 = Rectangle(0.0F, 0.0F, 0.5F, 1.0F)
        val rect2 = Rectangle(0.5F, 0.0F, 1.0F, 1.0F)

        assertTrue(rect1.intersect(rect2))
    }

    @Test
    fun intersect_horizontal2() {
        val rect1 = Rectangle(0.0F, 0.0F, 0.5F, 1.0F)
        val rect2 = Rectangle(0.51F, 0.0F, 1.0F, 1.0F)

        assertFalse(rect1.intersect(rect2))
    }

    @Test
    fun intersect_vertical1() {
        val rect1 = Rectangle(0.0F, 0.0F, 1.0F, 0.5F)
        val rect2 = Rectangle(0.0F, 0.5F, 1.0F, 1.0F)

        assertTrue(rect1.intersect(rect2))
    }

    @Test
    fun intersect_vertical2() {
        val rect1 = Rectangle(0.0F, 0.0F, 1.0F, 0.5F)
        val rect2 = Rectangle(0.0F, 0.51F, 1.0F, 1.0F)

        assertFalse(rect1.intersect(rect2))
    }

    @Test
    fun relativeBy1() {
        val rect1 = Rectangle(2.0F, 0.0F, 3.0F, 1.0F)
        val rect2 = Rectangle(2.5F, 0.25F, 2.8F, 0.75F)

        rect2.relativeBy(rect1)

        assertEquals(0.5F, rect2.left, 0.0001F)
        assertEquals(0.8F, rect2.right, 0.0001F)
        assertEquals(0.25F, rect2.top, 0.0001F)
        assertEquals(0.75F, rect2.bottom, 0.0001F)
    }

    @Test
    fun relativeBy2() {
        val rect1 = Rectangle(-2.0F, -1.0F, -3.0F, -2.0F)
        val rect2 = Rectangle(-2.5F, -1.25F, -3.8F, -2.75F)

        rect2.relativeBy(rect1)

        assertEquals(-0.5F, rect2.left, 0.0001F)
        assertEquals(-1.8F, rect2.right, 0.0001F)
        assertEquals(-0.25F, rect2.top, 0.0001F)
        assertEquals(-1.75F, rect2.bottom, 0.0001F)
    }

    @Test
    fun and1() {
        val rect1 = Rectangle(2.0F, 0.0F, 3.0F, 1.0F)
        val rect2 = Rectangle(2.5F, 0.25F, 2.8F, 0.75F)

        val result = Rectangle()
        val resultAnd = Rectangle.and(rect1, rect2, result)
        assertNotNull(resultAnd)

        assertEquals(2.5F, result.left, 0.0001F)
        assertEquals(2.8F, result.right, 0.0001F)
        assertEquals(0.25F, result.top, 0.0001F)
        assertEquals(0.75F, result.bottom, 0.0001F)
    }

    @Test
    fun and2() {
        val rect1 = Rectangle(2.81F, 0.0F, 5.0F, 1.0F)
        val rect2 = Rectangle(2.5F, 0.25F, 2.8F, 0.75F)

        val resultAnd = Rectangle.and(rect1, rect2)
        assertNull(resultAnd)
    }

    @Test
    fun or1() {
        val rect1 = Rectangle(2.0F, 0.0F, 3.0F, 1.0F)
        val rect2 = Rectangle(2.5F, 0.25F, 2.8F, 0.75F)

        val result = Rectangle()
        val resultAnd = Rectangle.or(rect1, rect2, result)
        assertNotNull(resultAnd)

        assertEquals(2.0F, result.left, 0.0001F)
        assertEquals(3.0F, result.right, 0.0001F)
        assertEquals(0.0F, result.top, 0.0001F)
        assertEquals(1.0F, result.bottom, 0.0001F)
    }

    @Test
    fun or2() {
        val rect1 = Rectangle(2.81F, 0.0F, 5.0F, 1.0F)
        val rect2 = Rectangle(2.5F, 0.25F, 2.8F, 0.75F)

        val resultAnd = Rectangle.or(rect1, rect2)
        assertNull(resultAnd)
    }

}
