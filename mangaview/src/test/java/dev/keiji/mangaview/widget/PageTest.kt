package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import dev.keiji.mangaview.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

class PageTest {

    companion object {
        private const val FLOAT_DELTA = 0.001F

        private const val VIEW_WIDTH = 1080F
        private const val VIEW_HEIGHT = 2048F
    }

    @Test
    fun layout_isCorrect() {
        val viewState = ViewContext(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalRtlLayoutManager()
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                    { _: ContentLayer, _: RectF -> }
                }.filter { !it }.count() == 0

                if (initialized) {
                    break
                }
            }
        }

        pages[0].also { page ->
            page.layers[0].also { layer ->
                assertTrue(layer.isPrepared)
                assertEquals(0.9375F, layer.baseScale, FLOAT_DELTA)
                assertEquals(0, layer.paddingLeft)
                assertEquals(0, layer.paddingRight)
                assertEquals(207, layer.paddingTop)
                assertEquals(207, layer.paddingBottom)
            }
        }
        pages[1].also { page ->
            page.layers[0].also { layer ->
                assertTrue(layer.isPrepared)
                assertEquals(0.94158673F, layer.baseScale, FLOAT_DELTA)
                assertEquals(0, layer.paddingLeft)
                assertEquals(0, layer.paddingRight)
                assertEquals(236, layer.paddingTop)
                assertEquals(236, layer.paddingBottom)
            }
        }
    }

    @Test
    fun draw_isCorrect() {
        val viewState = ViewContext(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalRtlLayoutManager(reversed = true)
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                    { _: ContentLayer, _: RectF -> }
                }.none { !it }

                if (initialized) {
                    break
                }
            }
        }

        viewState.offset(-VIEW_WIDTH / 2, -VIEW_HEIGHT / 2)
        assertEquals(
            Rectangle(left = -540.0F, top = 0.0F, right = 540.0F, bottom = 2048.0F),
            viewState.viewport
        )

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        val drawResult =
            pages[0].draw(
                null,
                viewState,
                paint,
                coroutineScope
            ) { _: ContentLayer, _: RectF -> }
                    &&
                    pages[1].draw(
                        null,
                        viewState,
                        paint,
                        coroutineScope
                    ) { _: ContentLayer, _: RectF -> }

        assertTrue(drawResult)

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 540.0F, bottom = 2048.0F),
            pages[0].pageViewport
        )
        assertEquals(
            Rectangle(left = 540.0F, top = 0.0F, right = 1080.0F, bottom = 2048.0F),
            pages[1].pageViewport
        )

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 540.0F, bottom = 2048.0F),
            pages[0].contentSrc
        )

        assertEquals(
            Rectangle(left = 540.0F, top = 0.0F, right = 1080.0F, bottom = 2048.0F),
            pages[1].contentSrc
        )

        assertEquals(
            Rectangle(left = 540.0F, top = 0.0F, right = 1080.0F, bottom = 2048.0F),
            pages[0].layers[0].destOnView
        )

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 540.0F, bottom = 2048.0F),
            pages[1].layers[0].destOnView
        )
    }

    @Test
    fun draw_isCorrect2() {
        val viewState = ViewContext(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalRtlLayoutManager()
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                    { _: ContentLayer, _: RectF -> }
                }.none { !it }

                if (initialized) {
                    break
                }
            }
        }

        viewState.offset(-VIEW_WIDTH / 4.0F, -VIEW_HEIGHT / 2.0F)
        assertEquals(
            Rectangle(left = -270.0F, top = 0.0F, right = 810.0F, bottom = 2048.0F),
            viewState.viewport
        )

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        val drawResult = pages[0].draw(
            null,
            viewState,
            paint,
            coroutineScope
        ) { _: ContentLayer, _: RectF -> }
                &&
                pages[1].draw(
                    null,
                    viewState,
                    paint,
                    coroutineScope
                ) { _: ContentLayer, _: RectF -> }

        assertTrue(drawResult)

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 810.0F, bottom = 2048.0F),
            pages[0].pageViewport
        )
        assertEquals(
            Rectangle(left = 810.0F, top = 0.0F, right = 1080.0F, bottom = 2048.0F),
            pages[1].pageViewport
        )

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 810.0F, bottom = 2048.0F),
            pages[0].contentSrc
        )

        assertEquals(
            Rectangle(left = 810.0F, top = 0.0F, right = 1080.0F, bottom = 2048.0F),
            pages[1].contentSrc
        )

        assertEquals(
            Rectangle(left = 270.0F, top = 0.0F, right = 1080.0F, bottom = 2048.0F),
            pages[0].layers[0].destOnView
        )

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 270.0F, bottom = 2048.0F),
            pages[1].layers[0].destOnView
        )
    }

    @Test
    fun scroll_isCorrect1() {
        val viewState = ViewContext(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalRtlLayoutManager()
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                    { _: ContentLayer, _: RectF -> }
                }.none { !it }

                if (initialized) {
                    break
                }
            }
        }

        viewState.offset(-VIEW_WIDTH, -VIEW_HEIGHT)

        assertEquals(
            Rectangle(left = -1080.0F, top = 0.0F, right = 0.0F, bottom = 2048.0F),
            viewState.viewport
        )

        viewState.offset(-VIEW_WIDTH / 2, -VIEW_HEIGHT / 2)

        assertEquals(
            Rectangle(left = -1080.0F, top = 0.0F, right = 0.0F, bottom = 2048.0F),
            viewState.viewport
        )

        viewState.offset(VIEW_WIDTH / 2, VIEW_HEIGHT / 2)

        assertEquals(
            Rectangle(left = -540.0F, top = 0.0F, right = 540.0F, bottom = 2048.0F),
            viewState.viewport
        )

    }

    @Test
    fun scale_isCorrect1() {
        val viewState = ViewContext(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalRtlLayoutManager()
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                    { _: ContentLayer, _: RectF -> }
                }.none { !it }

                if (initialized) {
                    break
                }
            }
        }

        viewState.setScale(
            2.0F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(2.0F, viewState.currentScale)
        assertEquals(540.0F, viewState.width, 0.0001F)
        assertEquals(1024.0F, viewState.height, 0.0001F)

        assertEquals(
            Rectangle(left = 270.0F, top = 512.0F, right = 810.0F, bottom = 1536.0F),
            viewState.viewport
        )

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        val drawResult = pages[0].draw(
            null,
            viewState,
            paint,
            coroutineScope,
        ) { _: ContentLayer, _: RectF -> }
                &&
                pages[1].draw(
                    null,
                    viewState,
                    paint,
                    coroutineScope
                ) { _: ContentLayer, _: RectF -> }
        assertTrue(drawResult)

        viewState.setScale(
            0.5F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(1.0F, viewState.currentScale)
        assertEquals(VIEW_WIDTH, viewState.width, 0.0001F)
        assertEquals(VIEW_HEIGHT, viewState.height, 0.0001F)

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 1080.0F, bottom = 2048.0F),
            viewState.viewport
        )

        viewState.setScale(
            0.5F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(1.0F, viewState.currentScale)
        assertEquals(VIEW_WIDTH, viewState.width, 0.0001F)
        assertEquals(VIEW_HEIGHT, viewState.height, 0.0001F)

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 1080.0F, bottom = 2048.0F),
            viewState.viewport
        )
    }

    @Test
    fun scale_isCorrect2() {
        val viewState = ViewContext(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalRtlLayoutManager()
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this) { _: ContentLayer, _: RectF -> }
                }.none { !it }

                if (initialized) {
                    break
                }
            }
        }

        viewState.setScale(
            2.0F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(2.0F, viewState.currentScale)

        assertEquals(
            Rectangle(left = 270.0F, top = 512.0F, right = 810.0F, bottom = 1536.0F),
            viewState.viewport
        )

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        val drawResult = pages[0].draw(
            null,
            viewState,
            paint,
            coroutineScope,
        ) { _: ContentLayer, _: RectF -> }
                &&
                pages[1].draw(
                    null,
                    viewState,
                    paint,
                    coroutineScope
                ) { _: ContentLayer, _: RectF -> }
        assertTrue(drawResult)

        viewState.setScale(
            0.5F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(1.0F, viewState.currentScale)

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 1080.0F, bottom = 2048.0F),
            viewState.viewport
        )
    }

    @Test
    fun scroll_isCorrect2() {
        val viewState = ViewContext(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalRtlLayoutManager()
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        viewState.setScale(
            2.0F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(540.0F, viewState.width, 0.0001F)
        assertEquals(1024.0F, viewState.height, 0.0001F)
        assertEquals(
            Rectangle(left = 270.0F, top = 512.0F, right = 810.0F, bottom = 1536.0F),
            viewState.viewport
        )

        viewState.offset(-VIEW_WIDTH, -VIEW_HEIGHT)

        assertEquals(
            Rectangle(left = -810.0F, top = 0.0F, right = -270.0F, bottom = 1024.0F),
            viewState.viewport
        )

        viewState.offset(-VIEW_WIDTH / 2, 0.0F)

        assertEquals(
            Rectangle(left = -1080.0F, top = 0.0F, right = -540.0F, bottom = 1024.0F),
            viewState.viewport
        )

        viewState.offset(0.0F, VIEW_HEIGHT / 2)

        assertEquals(
            Rectangle(left = -1080.0F, top = 1024.0F, right = -540.0F, bottom = 2048.0F),
            viewState.viewport
        )

        viewState.setScale(
            1.5F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )

        assertEquals(1.5F, viewState.currentScale, FLOAT_DELTA)
        assertEquals(720.0F, viewState.width, FLOAT_DELTA)
        assertEquals(1365.3335F, viewState.height, FLOAT_DELTA)
        assertEquals(
            Rectangle(left = -1080.0F, top = 682.66656F, right = -360.0F, bottom = 2048.0F),
            viewState.viewport
        )

        viewState.setScale(
            2.0F,
            viewState.viewWidth / 2.0F,
            viewState.viewHeight / 2.0F
        )

        assertEquals(2.0F, viewState.currentScale)
        assertEquals(540.0F, viewState.width, FLOAT_DELTA)
        assertEquals(1024.0F, viewState.height, FLOAT_DELTA)
        assertEquals(
            Rectangle(left = -990.0F, top = 853.33325F, right = -450.0F, bottom = 1877.3333F),
            viewState.viewport
        )

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                    { _: ContentLayer, _: RectF -> }
                }.none { !it }

                if (initialized) {
                    break
                }
            }
        }

        assertEquals(
            Rectangle(left = 90.0F, top = 853.33325F, right = 630.0F, bottom = 1877.3333F),
            pages[1].contentSrc
        )

    }

    @Test
    fun scroll_isCorrect3() {
        val viewState = ViewContext(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalRtlLayoutManager()
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        viewState.offset(-VIEW_WIDTH / 2, 0.0F)

        assertEquals(
            Rectangle(left = -540.0F, top = 0.0F, right = 540.0F, bottom = 2048.0F),
            viewState.viewport
        )

        viewState.setScale(
            2.0F,
            viewState.viewWidth / 2.0F,
            viewState.viewHeight / 2.0F
        )
        assertEquals(2.0F, viewState.currentScale)
        assertEquals(540.0F, viewState.width, FLOAT_DELTA)
        assertEquals(1024.0F, viewState.height, FLOAT_DELTA)

        assertEquals(
            Rectangle(left = -270.0F, top = 512.0F, right = 270.0F, bottom = 1536.0F),
            viewState.viewport
        )

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                    { _: ContentLayer, _: RectF -> }
                }.none { !it }

                if (initialized) {
                    break
                }
            }
        }

        assertEquals(
            Rectangle(left = 540.0F, top = 0.0F, right = 1080.0F, bottom = 2048.0F),
            pages[0].displayProjection
        )
        assertEquals(
            Rectangle(left = 0.0F, top = 512.0F, right = 270.0F, bottom = 1536.0F),
            pages[0].contentSrc
        )

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 540.0F, bottom = 2048.0F),
            pages[1].displayProjection
        )
        assertEquals(
            Rectangle(left = 810.0F, top = 512.0F, right = 1080.0F, bottom = 1536.0F),
            pages[1].contentSrc
        )
    }
}

private fun createDummyPages(): List<Page> {
    val pages = (0 until 2).map {
        Page(it)
    }

    pages[0].also { page ->
        page.layers.add(DummyLayer(1152F, 1771F))
    }
    pages[1].also { page ->
        page.layers.add(DummyLayer(1147F, 1703F))
    }

    return pages
}

fun assertEquals(expected: Rectangle, actual: Rectangle, delta: Float) {
    assertEquals(expected.left, actual.left, delta)
    assertEquals(expected.top, actual.top, delta)
    assertEquals(expected.right, actual.right, delta)
    assertEquals(expected.bottom, actual.bottom, delta)
}

class DummyLayer(
    private val cWidth: Float,
    private val cHeight: Float
) : ContentLayer() {

    override val contentWidth: Float
        get() = cWidth
    override val contentHeight: Float
        get() = cHeight

    private var isContentLoaded = false

    override val isPrepared: Boolean
        get() = isContentLoaded

    override fun onDraw(
        canvas: Canvas?,
        srcRect: Rect,
        dstRect: RectF,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean = true

    override fun prepareContent(viewContext: ViewContext, page: Page) {
        isContentLoaded = true
    }

}
