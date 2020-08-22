package jp.co.c_lis.mangaview.android.widget

import android.graphics.Canvas
import android.graphics.Paint
import jp.co.c_lis.mangaview.android.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

abstract class ContentLayer {

    companion object {
        private val TAG = ContentLayer::class.java.simpleName
    }

    abstract val contentWidth: Float
    abstract val contentHeight: Float

    internal var minScale: Float = 1.0F

    var paddingLeft = 0
    var paddingTop = 0
    var paddingRight = 0
    var paddingBottom = 0

    val contentSrc = Rectangle()
    val projection = Rectangle()

    abstract suspend fun prepareContent(viewState: ViewState, page: Page)

    open val isPrepared
        get() = false

    private suspend fun prepare(viewState: ViewState, page: Page) {
        prepareContent(viewState, page)

        minScale = min(
            page.position.width / contentWidth,
            page.position.height / contentHeight
        )

        val scaledContentWidth = contentWidth * minScale
        val scaledContentHeight = contentHeight * minScale

        val paddingHorizontal = page.position.width - scaledContentWidth
        val paddingVertical = page.position.height - scaledContentHeight

        val left = paddingHorizontal / 2
        val right = paddingHorizontal - left
        val top = paddingVertical / 2
        val bottom = paddingVertical - top

        paddingLeft = (left / minScale).roundToInt()
        paddingRight = (right / minScale).roundToInt()
        paddingTop = (top / minScale).roundToInt()
        paddingBottom = (bottom / minScale).roundToInt()
    }

    private var preparing: Job? = null

    fun draw(
        canvas: Canvas?,
        viewState: ViewState,
        page: Page,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {
        contentSrc.set(page.contentSrc)
        projection.set(page.projection)

        if (!isPrepared && preparing == null) {
            preparing = coroutineScope.launch(Dispatchers.IO) {
                prepare(viewState, page)
                preparing = null
            }
            return false
        }

        return onDraw(canvas, viewState, paint, coroutineScope)
    }

    abstract fun onDraw(
        canvas: Canvas?,
        viewState: ViewState,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean

    open fun recycle() {
    }
}
