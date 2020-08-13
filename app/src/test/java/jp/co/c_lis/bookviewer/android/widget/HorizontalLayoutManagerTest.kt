package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Rectangle
import org.junit.Test

import org.junit.Assert.*

class HorizontalLayoutManagerTest {

    @Test
    fun layout_isCorrect() {
        val viewState = ViewState(1080F, 2048F)
        val layoutManager = HorizontalLayoutManager(reversed = false)

        layoutManager.pageList = (0 until 5).map { Page(it) }
        layoutManager.layout(viewState)

        assertEquals(
            layoutManager.pageList[0].position,
            Rectangle(0.0F, 0.0F, 1.0F, 1.0F)
        )
        assertEquals(
            layoutManager.pageList[1].position,
            Rectangle(1.0F, 0.0F, 2.0F, 1.0F)
        )
        assertEquals(
            layoutManager.pageList[2].position,
            Rectangle(2.0F, 0.0F, 3.0F, 1.0F)
        )
        assertEquals(
            layoutManager.pageList[3].position,
            Rectangle(3.0F, 0.0F, 4.0F, 1.0F)
        )
        assertEquals(
            layoutManager.pageList[4].position,
            Rectangle(4.0F, 0.0F, 5.0F, 1.0F)
        )
    }

    @Test
    fun layout_reverse_isCorrect() {
        val viewState = ViewState(1080F, 2048F)
        val layoutManager = HorizontalLayoutManager(reversed = true)

        layoutManager.pageList = (0 until 5).map { Page(it) }
        layoutManager.layout(viewState)

        assertEquals(
            Rectangle(0.0F, 0.0F, 1.0F, 1.0F),
            layoutManager.pageList[0].position
        )
        assertEquals(
            Rectangle(-1.0F, 0.0F, 0.0F, 1.0F),
            layoutManager.pageList[1].position
        )
        assertEquals(
            Rectangle(-2.0F, 0.0F, -1.0F, 1.0F),
            layoutManager.pageList[2].position
        )
        assertEquals(
            Rectangle(-3.0F, 0.0F, -2.0F, 1.0F),
            layoutManager.pageList[3].position
        )
        assertEquals(
            Rectangle(-4.0F, 0.0F, -3.0F, 1.0F),
            layoutManager.pageList[4].position
        )

        assertEquals(
            Rectangle(-4.0F, 0.0F, 1.0F, 1.0F),
            viewState.scrollableArea
        )
    }
}
