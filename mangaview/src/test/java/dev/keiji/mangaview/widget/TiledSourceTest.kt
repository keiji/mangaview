package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Rectangle
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TiledSourceTest {

    @Test
    fun build_isCorrect1() {
        val source = TiledSource.build(
            1000.0F,
            2000.0F,
            256.0F,
            256.0F
        )
        Assert.assertEquals(4, source.colCount)
        Assert.assertEquals(8, source.rowCount)

        Assert.assertEquals(32, source.tileList.size)

        Assert.assertEquals(
            Rectangle(left = 768.0F, top = 0.0F, right = 1000.0F, bottom = 256.0F),
            source.tileList[3].position
        )
        Assert.assertEquals(
            Rectangle(left = 768.0F, top = 1792.0F, right = 1000.0F, bottom = 2000.0F),
            source.tileList[31].position
        )
    }

    @Test
    fun build_isCorrect2() {
        val source = TiledSource.build(
            1000.0F,
            2000.0F,
            256.0F,
            256.0F,
            250.0F,
            250.0F
        )
        Assert.assertEquals(4, source.colCount)
        Assert.assertEquals(8, source.rowCount)

        Assert.assertEquals(32, source.tileList.size)

        Assert.assertEquals(
            Rectangle(left = 750.0F, top = 0.0F, right = 1000.0F, bottom = 256.0F),
            source.tileList[3].position
        )
        Assert.assertEquals(
            Rectangle(left = 750.0F, top = 1750.0F, right = 1000.0F, bottom = 2000.0F),
            source.tileList[31].position
        )
    }

    @Test
    fun error1() {
        try {
            val source = TiledSource.build(
                1000.0F,
                2000.0F,
                1001.0F,
                256.0F
            )
            Assert.fail()
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun error2() {
        try {
            val source = TiledSource.build(
                1000.0F,
                2000.0F,
                256.0F,
                2001.0F
            )
            Assert.fail()
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun error3() {
        try {
            val source = TiledSource.build(
                1000.0F,
                2000.0F,
                256.0F,
                256.0F,
                256.1F,
                256.0F
            )
            Assert.fail()
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun error4() {
        try {
            val source = TiledSource.build(
                1000.0F,
                2000.0F,
                256.0F,
                256.0F,
                256.0F,
                256.1F
            )
            Assert.fail()
        } catch (e: IllegalArgumentException) {
        }
    }
}
