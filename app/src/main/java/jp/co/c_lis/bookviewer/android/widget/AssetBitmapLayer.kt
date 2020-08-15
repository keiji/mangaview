package jp.co.c_lis.bookviewer.android.widget

import android.content.res.AssetManager
import android.graphics.*
import android.util.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt

class AssetBitmapLayer(
    private val assetManager: AssetManager,
    private val fileName: String,
    alignHorizontal: PageHorizontalAlign = PageHorizontalAlign.Center,
    alignVertical: PageVerticalAlign = PageVerticalAlign.Middle
) : ContentLayer(alignHorizontal, alignVertical) {

    companion object {
        private val TAG = AssetBitmapLayer::class.java.simpleName
    }

    private var bitmap: Bitmap? = null

    override val contentWidth: Float
        get() = bitmap?.width?.toFloat() ?: 0.0F
    override val contentHeight: Float
        get() = bitmap?.height?.toFloat() ?: 0.0F

    override val isPrepared: Boolean
        get() = bitmap != null

    override suspend fun prepareContent(viewState: ViewState, page: Page) {
        bitmap = assetManager.open(fileName).use {
            BitmapFactory.decodeStream(it)
        }
    }

    private val srcRect = Rect()
    private val dstRect = RectF()

    override fun onDraw(
        canvas: Canvas?,
        viewState: ViewState,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {
        val bitmapSnapshot = bitmap ?: return false

        srcRect.also {
            it.left = (contentSrc.left / minScale).roundToInt() - paddingLeft
            it.right = (contentSrc.right / minScale).roundToInt() - paddingRight
            it.top = (contentSrc.top / minScale).roundToInt() - paddingTop
            it.bottom = (contentSrc.bottom / minScale).roundToInt() - paddingBottom
        }
        dstRect.also {
            it.left = destOnView.left
            it.right = destOnView.right
            it.top = destOnView.top
            it.bottom = destOnView.bottom
        }

        canvas?.drawBitmap(
            bitmapSnapshot,
            srcRect,
            dstRect,
            paint
        )

        return true
    }
}
