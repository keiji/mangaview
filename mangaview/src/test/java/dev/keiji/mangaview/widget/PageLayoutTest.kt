package dev.keiji.mangaview.widget

import android.graphics.Paint
import android.graphics.RectF
import dev.keiji.mangaview.Rectangle
import dev.keiji.mangaview.layer.ContentLayer
import dev.keiji.mangaview.layout.HorizontalRtlLayoutManager
import dev.keiji.mangaview.layout.LayoutManager
import dev.keiji.mangaview.layout.PageLayoutManager
import dev.keiji.mangaview.layout.SinglePageLayoutManager
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import kotlin.math.roundToInt

@RunWith(MockitoJUnitRunner::class)
class PageLayoutTest {

    companion object {
        private const val FLOAT_DELTA = 0.001F

        private const val VIEW_WIDTH = 1080F
        private const val VIEW_HEIGHT = 2048F

        private const val PAGE_WIDTH = 859
        private const val PAGE_HEIGHT = 1214
    }

    private val paint = Paint()

    data class Prime(
        val viewContext: ViewContext,
        val layoutManager: LayoutManager,
        val pageLayoutManager: PageLayoutManager,
        val pageAdapter: PageAdapter
    )

    private fun init(): Prime {
        val viewContext = ViewContext().also {
            it.setViewSize(VIEW_WIDTH.roundToInt(), VIEW_HEIGHT.roundToInt())
        }
        val adapter = DummyAdapter(PAGE_WIDTH, PAGE_HEIGHT)
        val layoutManager = HorizontalRtlLayoutManager()
        val pageLayoutManager = SinglePageLayoutManager()

        layoutManager.pageLayoutManager = pageLayoutManager
        layoutManager.adapter = adapter
        pageLayoutManager.pageAdapter = adapter
        layoutManager.initWith(viewContext)

        return Prime(viewContext, layoutManager, pageLayoutManager, adapter)
    }

    @Test
    fun pageLayout_isCorrect() {
        val (viewContext, layoutManager, pageLayoutManager, adapter) = init()

        val pageLayoutList = layoutManager.obtainVisiblePageLayout(
            viewContext,
            offsetScreenPageLimit = adapter.pageCount
        )

        assertEquals(4, pageLayoutList.size)
        assertEquals(listOf(0, 1, 2, 3), pageLayoutList.flatMap { it.pages }.map { it.index })

        while (true) {
            val initialized = pageLayoutList
                .flatMap { it.pages }
                .map {
                    it.draw(null, viewContext, paint)
                    { _: ContentLayer, _: RectF -> }
                }.none { !it }

            if (initialized) {
                break
            }
        }

        pageLayoutList[0].also { pageLayout ->
            pageLayout.scrollArea.also { scrollArea ->
                assertEquals(-VIEW_WIDTH, scrollArea.left)
                assertEquals(260.8335F, scrollArea.top)
                assertEquals(0.0F, scrollArea.right)
                assertEquals((VIEW_HEIGHT - 260.8335F), scrollArea.bottom)
            }
            assertEquals(1, pageLayout.pages.size)
            assertEquals(
                Rectangle(left = -VIEW_WIDTH, top = 0.0F, right = 0.0F, bottom = VIEW_HEIGHT),
                pageLayout.globalPosition
            )
            assertEquals(0, pageLayout.keyPage?.index)
            assertEquals(true, pageLayout.isFilled)

            pageLayout.pages[0].also { page ->
                assertEquals(0, page.index)

                assertEquals(PAGE_WIDTH, page.width)
                assertEquals(PAGE_HEIGHT, page.height)

                assertEquals(1.2572759F, page.baseScale)
                assertEquals(1080.0F, page.scaledWidth)
                assertEquals(1526.333F, page.scaledHeight)

                assertEquals(
                    Rectangle(left = -1080.0F, top = 260.8335F, right = 0.0F, bottom = 1787.1665F),
                    page.globalRect
                )
                assertEquals(
                    Rectangle(left = 0.0F, top = 260.8335F, right = 1080.0F, bottom = 1787.1665F),
                    page.displayProjection
                )
                assertEquals(
                    Rectangle(left = 0.0F, top = 0.0F, right = 1080.0F, bottom = 1526.333F),
                    page.contentSrc
                )

                page.layers[0].also { layer ->
                    assertEquals(1.2572759F, layer.baseScale, FLOAT_DELTA)
                    assertEquals(0.0F, layer.paddingLeft, FLOAT_DELTA)
                    assertEquals(0.0F, layer.paddingTop, FLOAT_DELTA)
                    assertEquals(0.0F, layer.paddingRight, FLOAT_DELTA)
                    assertEquals(0.0F, layer.paddingBottom, FLOAT_DELTA)

                    assertEquals(
                        Rectangle(
                            left = -1080.0F,
                            top = 260.8335F,
                            right = 0.0F,
                            bottom = 1787.1665F
                        ),
                        layer.globalPosition
                    )
                    assertEquals(
                        Rectangle(left = 0.0F, top = 0.0F, right = 859.0F, bottom = 1214.0F),
                        layer.contentSrc
                    )
                }
            }
        }
        pageLayoutList[1].also { pageLayout ->
            pageLayout.scrollArea.also { scrollArea ->
                assertEquals(-VIEW_WIDTH * 2, scrollArea.left)
                assertEquals(260.8335F, scrollArea.top)
                assertEquals(-VIEW_WIDTH, scrollArea.right)
                assertEquals((VIEW_HEIGHT - 260.8335F), scrollArea.bottom)
            }
            assertEquals(1, pageLayout.pages.size)
            assertEquals(
                Rectangle(
                    left = -VIEW_WIDTH * 2,
                    top = 0.0F,
                    right = -VIEW_WIDTH,
                    bottom = VIEW_HEIGHT
                ),
                pageLayout.globalPosition
            )
            assertEquals(1, pageLayout.keyPage?.index)
            assertEquals(true, pageLayout.isFilled)

            pageLayout.pages[0].also { page ->
                assertEquals(1, page.index)

                assertEquals(PAGE_WIDTH, page.width)
                assertEquals(PAGE_HEIGHT, page.height)

                assertEquals(1.2572759F, page.baseScale)
                assertEquals(1080.0F, page.scaledWidth)
                assertEquals(1526.333F, page.scaledHeight)

                assertEquals(
                    Rectangle(
                        left = -2160.0F,
                        top = 260.8335F,
                        right = -1080.0F,
                        bottom = 1787.1665F
                    ),
                    page.globalRect
                )
                assertEquals(
                    Rectangle(left = 0.0F, top = 260.8335F, right = 0.0F, bottom = 1787.1665F),
                    page.displayProjection
                )
                assertEquals(
                    Rectangle(left = 1080.0F, top = 0.0F, right = 1080.0F, bottom = 1526.333F),
                    page.contentSrc
                )

                page.layers[0].also { layer ->
                    assertEquals(1.2572759F, layer.baseScale, FLOAT_DELTA)
                    assertEquals(0.0F, layer.paddingLeft, FLOAT_DELTA)
                    assertEquals(0.0F, layer.paddingTop, FLOAT_DELTA)
                    assertEquals(0.0F, layer.paddingRight, FLOAT_DELTA)
                    assertEquals(0.0F, layer.paddingBottom, FLOAT_DELTA)

                    assertEquals(
                        Rectangle(
                            left = -2160.0F,
                            top = 260.8335F,
                            right = -1080.0F,
                            bottom = 1787.1665F
                        ),
                        layer.globalPosition
                    )
                    assertEquals(
                        Rectangle(left = 859.0F, top = 0.0F, right = 859.0F, bottom = 1214.0F),
                        layer.contentSrc
                    )
                }
            }
        }
    }
}
