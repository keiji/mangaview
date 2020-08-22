package jp.co.c_lis.mangaview.widget

import jp.co.c_lis.mangaview.android.Rectangle
import org.junit.Test

import org.junit.Assert.*

class HorizontalLayoutManagerTest {

    companion object {
        private const val VIEW_WIDTH = 1080F
        private const val VIEW_HEIGHT = 2048F
    }

    @Test
    fun layout_isCorrect() {
        val viewState = ViewContext(VIEW_WIDTH, VIEW_HEIGHT)
        val layoutManager = HorizontalLayoutManager(reversed = false)

        layoutManager.pageList = (0 until 5).map { Page(it) }
        layoutManager.layout(viewState)

        assertEquals(
            layoutManager.pageList[0].position,
            Rectangle(0 * VIEW_WIDTH, 0.0F, VIEW_WIDTH, VIEW_HEIGHT)
        )
        assertEquals(
            layoutManager.pageList[1].position,
            Rectangle(1 * VIEW_WIDTH, 0.0F, 2 * VIEW_WIDTH, VIEW_HEIGHT)
        )
        assertEquals(
            layoutManager.pageList[2].position,
            Rectangle(2 * VIEW_WIDTH, 0.0F, 3 * VIEW_WIDTH, VIEW_HEIGHT)
        )
        assertEquals(
            layoutManager.pageList[3].position,
            Rectangle(3 * VIEW_WIDTH, 0.0F, 4 * VIEW_WIDTH, VIEW_HEIGHT)
        )
        assertEquals(
            layoutManager.pageList[4].position,
            Rectangle(4 * VIEW_WIDTH, 0.0F, 5 * VIEW_WIDTH, VIEW_HEIGHT)
        )
    }

    @Test
    fun layout_reverse_isCorrect() {
        val viewState = ViewContext(VIEW_WIDTH, VIEW_HEIGHT)
        val layoutManager = HorizontalLayoutManager(reversed = true)

        layoutManager.pageList = (0 until 5).map { Page(it) }
        layoutManager.layout(viewState)

        assertEquals(
            Rectangle(0.0F, 0.0F, 1 * VIEW_WIDTH, VIEW_HEIGHT),
            layoutManager.pageList[0].position
        )
        assertEquals(
            Rectangle(-1 * VIEW_WIDTH, 0.0F, 0.0F, VIEW_HEIGHT),
            layoutManager.pageList[1].position
        )
        assertEquals(
            Rectangle(-2 * VIEW_WIDTH, 0.0F, -1 * VIEW_WIDTH, VIEW_HEIGHT),
            layoutManager.pageList[2].position
        )
        assertEquals(
            Rectangle(-3 * VIEW_WIDTH, 0.0F, -2 * VIEW_WIDTH, VIEW_HEIGHT),
            layoutManager.pageList[3].position
        )
        assertEquals(
            Rectangle(-4 * VIEW_WIDTH, 0.0F, -3 * VIEW_WIDTH, VIEW_HEIGHT),
            layoutManager.pageList[4].position
        )
        assertEquals(
            Rectangle(left = -4320.0F, top = 0.0F, right = 1080.0F, bottom = 2048.0F),
            viewState.scrollableArea
        )
    }
}
