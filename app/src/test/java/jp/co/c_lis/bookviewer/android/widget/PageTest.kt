package jp.co.c_lis.bookviewer.android.widget

import android.graphics.Canvas
import android.graphics.Paint
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

class PageTest {

    companion object {
        private const val FLOAT_DELTA = 0.0001F

        private const val VIEW_WIDTH = 1080F
        private const val VIEW_HEIGHT = 2048F
    }

    @Test
    fun layout_isCorrect() {
        val viewState = ViewState(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalLayoutManager(reversed = true)
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                }.filter { !it }.count() == 0

                if (initialized) {
                    break
                }
            }
        }

        pages[0].also { page ->
            page.layers[0].also { layer ->
                assertTrue(layer.isPrepared)
                assertEquals(0.9375F, layer.minScale, FLOAT_DELTA)
                assertEquals(
                    Rectangle(left = 0.0F, top = 0.09465027F, right = 1.0F, bottom = 0.90534973F),
                    layer.contentPosition,
                    FLOAT_DELTA
                )
            }
        }
        pages[1].also { page ->
            page.layers[0].also { layer ->
                assertTrue(layer.isPrepared)
                assertEquals(0.94158673F, layer.minScale, FLOAT_DELTA)
                assertEquals(
                    Rectangle(left = 0.0F, top = 0.108515084F, right = 1.0F, bottom = 0.8914849F),
                    layer.contentPosition,
                    FLOAT_DELTA
                )
            }
        }
    }

    @Test
    fun draw_isCorrect() {
        val viewState = ViewState(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalLayoutManager(reversed = true)
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                }.filter { !it }.count() == 0

                if (initialized) {
                    break
                }
            }
        }

        viewState.onScroll(-1.0F / 2.0F, -1.0F / 2.0F)
        assertEquals(
            Rectangle(-0.5F, 0.0F, 0.5F, 1.0F),
            viewState.viewport
        )

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        val drawResult = pages[0].draw(null, viewState, paint, coroutineScope)
                && pages[1].draw(null, viewState, paint, coroutineScope)
        assertTrue(drawResult)

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 0.5F, bottom = 1.0F),
            pages[0].pageViewport
        )
        assertEquals(
            Rectangle(left = 0.5F, top = 0.0F, right = 1.0F, bottom = 1.0F),
            pages[1].pageViewport
        )

        assertEquals(
            Rectangle(left = 0.5F, top = 0.0F, right = 1.0F, bottom = 1.0F),
            pages[0].destOnView
        )

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 0.5F, bottom = 1.0F),
            pages[1].destOnView
        )

        assertEquals(
            Rectangle(left = 0.5F, top = 0.09465027F, right = 1.0F, bottom = 0.90534973F),
            pages[0].layers[0].contentDst
        )

        assertEquals(
            Rectangle(left = 0.0F, top = 0.108515084F, right = 0.5F, bottom = 0.8914849F),
            pages[1].layers[0].contentDst
        )
    }

    @Test
    fun draw_isCorrect2() {
        val viewState = ViewState(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalLayoutManager(reversed = true)
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                }.filter { !it }.count() == 0

                if (initialized) {
                    break
                }
            }
        }

        viewState.onScroll(-1.0F / 4.0F, -1.0F / 2.0F)
        assertEquals(
            Rectangle(-0.25F, 0.0F, 0.75F, 1.0F),
            viewState.viewport
        )

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        val drawResult = pages[0].draw(null, viewState, paint, coroutineScope)
                && pages[1].draw(null, viewState, paint, coroutineScope)
        assertTrue(drawResult)

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 0.75F, bottom = 1.0F),
            pages[0].pageViewport
        )
        assertEquals(
            Rectangle(left = 0.75F, top = 0.0F, right = 1.0F, bottom = 1.0F),
            pages[1].pageViewport
        )

        assertEquals(
            Rectangle(left = 0.25F, top = 0.0F, right = 1.0F, bottom = 1.0F),
            pages[0].destOnView
        )

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 0.25F, bottom = 1.0F),
            pages[1].destOnView
        )

        assertEquals(
            Rectangle(left = 0.25F, top = 0.09465027F, right = 1.0F, bottom = 0.90534973F),
            pages[0].layers[0].contentDst
        )

        assertEquals(
            Rectangle(left = 0.0F, top = 0.108515084F, right = 0.25F, bottom = 0.8914849F),
            pages[1].layers[0].contentDst
        )
    }

    @Test
    fun scroll_isCorrect1() {
        val viewState = ViewState(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalLayoutManager(reversed = true)
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                }.filter { !it }.count() == 0

                if (initialized) {
                    break
                }
            }
        }

        viewState.onScroll(-1.0F, -1.0F)

        assertEquals(
            Rectangle(left = -1.0F, top = 0.0F, right = 0.0F, bottom = 1.0F),
            viewState.viewport
        )

        viewState.onScroll(-1.0F / 2, -1.0F / 2)

        assertEquals(
            Rectangle(left = -1.0F, top = 0.0F, right = 0.0F, bottom = 1.0F),
            viewState.viewport
        )

        viewState.onScroll(1.0F / 2, 1.0F / 2)

        assertEquals(
            Rectangle(left = -0.5F, top = 0.0F, right = 0.5F, bottom = 1.0F),
            viewState.viewport
        )

    }

    @Test
    fun scale_isCorrect1() {
        val viewState = ViewState(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalLayoutManager(reversed = true)
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                }.filter { !it }.count() == 0

                if (initialized) {
                    break
                }
            }
        }

        viewState.onScale(
            2.0F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(2.0F, viewState.scale)
        assertEquals(viewState.viewport.width, 0.5F, 0.0001F)
        assertEquals(viewState.viewport.height, 0.5F, 0.0001F)

        assertEquals(
            Rectangle(left = 0.25F, top = 0.25F, right = 0.75F, bottom = 0.75F),
            viewState.viewport
        )

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        val drawResult = pages[0].draw(null, viewState, paint, coroutineScope)
                && pages[1].draw(null, viewState, paint, coroutineScope)
        assertTrue(drawResult)

        viewState.onScale(
            0.5F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(1.0F, viewState.scale)
        assertEquals(1.0F, viewState.viewport.width, 0.0001F)
        assertEquals(1.0F, viewState.viewport.height, 0.0001F)

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 1.0F, bottom = 1.0F),
            viewState.viewport
        )

        viewState.onScale(
            0.5F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(1.0F, viewState.scale)
        assertEquals(1.0F, viewState.viewport.width, 0.0001F)
        assertEquals(1.0F, viewState.viewport.height, 0.0001F)

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 1.0F, bottom = 1.0F),
            viewState.viewport
        )
    }

    @Test
    fun scale_isCorrect2() {
        val viewState = ViewState(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalLayoutManager(reversed = true)
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                }.filter { !it }.count() == 0

                if (initialized) {
                    break
                }
            }
        }

        viewState.onScale(
            2.0F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(2.0F, viewState.scale)

        assertEquals(
            Rectangle(left = 0.25F, top = 0.25F, right = 0.75F, bottom = 0.75F),
            viewState.viewport
        )

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        val drawResult = pages[0].draw(null, viewState, paint, coroutineScope)
                && pages[1].draw(null, viewState, paint, coroutineScope)
        assertTrue(drawResult)

        viewState.onScale(
            0.5F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(1.0F, viewState.scale)

        assertEquals(
            Rectangle(left = 0.0F, top = 0.0F, right = 1.0F, bottom = 1.0F),
            viewState.viewport
        )
    }

    @Test
    fun scroll_isCorrect2() {
        val viewState = ViewState(VIEW_WIDTH, VIEW_HEIGHT)
        val paint = Paint()

        val layoutManager = HorizontalLayoutManager(reversed = true)
        val pages = createDummyPages()

        layoutManager.pageList = pages
        layoutManager.layout(viewState)

        viewState.onScale(
            2.0F,
            VIEW_WIDTH / 2.0F,
            VIEW_HEIGHT / 2.0F
        )
        assertEquals(0.5F, viewState.viewport.width, 0.0001F)
        assertEquals(0.5F, viewState.viewport.height, 0.0001F)
        assertEquals(
            Rectangle(left = 0.25F, top = 0.25F, right = 0.75F, bottom = 0.75F),
            viewState.viewport
        )

        viewState.onScroll(-1.0F, -1.0F)

        assertEquals(
            Rectangle(left = -0.75F, top = 0.0F, right = -0.25F, bottom = 0.5F),
            viewState.viewport
        )

        viewState.onScroll(-1.0F / 2, 0.0F)

        assertEquals(
            Rectangle(left = -1.0F, top = 0.0F, right = -0.5F, bottom = 0.5F),
            viewState.viewport
        )

        viewState.onScroll(0.0F, 1.0F / 2)

        assertEquals(
            Rectangle(left = -1.0F, top = 0.5F, right = -0.5F, bottom = 1.0F),
            viewState.viewport
        )

        viewState.onScale(
            1.5F,
            viewState.viewWidth / 2.0F,
            viewState.viewHeight / 2.0F
        )

        assertEquals(3.0F, viewState.scale, FLOAT_DELTA)
        assertEquals(0.33333337F, viewState.viewport.width, FLOAT_DELTA)
        assertEquals(0.33333337F, viewState.viewport.height, FLOAT_DELTA)
        assertEquals(
            Rectangle(left = -0.9583333F, top = 0.5416667F, right = -0.625F, bottom = 0.875F),
            viewState.viewport
        )

        viewState.setScale(
            2.0F,
            viewState.viewWidth / 2.0F,
            viewState.viewHeight / 2.0F
        )

        assertEquals(2.0F, viewState.scale)
        assertEquals(0.5F, viewState.viewport.width, FLOAT_DELTA)
        assertEquals(0.5F, viewState.viewport.height, FLOAT_DELTA)
        assertEquals(
            Rectangle(left = -0.9861111F, top = 0.49999994F, right = -0.4861111F, bottom = 1.0F),
            viewState.viewport
        )

        runBlocking {
            while (true) {
                val initialized = pages.map {
                    it.draw(null, viewState, paint, this)
                }.filter { !it }.count() == 0

                if (initialized) {
                    break
                }
            }
        }

        assertEquals(
            Rectangle(left = -0.9861111F, top = 0.49999994F, right = -0.4861111F, bottom = 1.0F),
            pages[0].destOnView
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

private fun assertEquals(expected: Rectangle, actual: Rectangle, delta: Float) {
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

    override suspend fun prepareContent(viewState: ViewState, pageRect: Rectangle) {
        isContentLoaded = true
    }

    override fun onDraw(
        canvas: Canvas?,
        viewState: ViewState,
        page: Page,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {
        return true
    }

}
