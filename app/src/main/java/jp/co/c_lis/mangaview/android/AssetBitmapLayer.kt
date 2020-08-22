package jp.co.c_lis.mangaview.android

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import jp.co.c_lis.mangaview.widget.ContentLayer
import jp.co.c_lis.mangaview.widget.Page
import jp.co.c_lis.mangaview.widget.ViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class AssetBitmapLayer(
    private val assetManager: AssetManager,
    private val fileName: String,
) : ContentLayer() {

    companion object {
        private val TAG = AssetBitmapLayer::class.java.simpleName
    }

    @Volatile
    private var bitmap: Bitmap? = null

    override val contentWidth: Float
        get() = bitmap?.width?.toFloat() ?: 0.0F
    override val contentHeight: Float
        get() = bitmap?.height?.toFloat() ?: 0.0F

    override val isPrepared: Boolean
        get() = bitmap != null

    override suspend fun prepareContent(viewState: ViewState, page: Page) =
        withContext(Dispatchers.IO) {
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
            it.left = contentSrc.left.roundToInt()
            it.right = contentSrc.right.roundToInt()
            it.top = contentSrc.top.roundToInt()
            it.bottom = contentSrc.bottom.roundToInt()
            it.offset(-paddingLeft, -paddingTop)
        }

        dstRect.also {
            it.left = projection.left
            it.right = projection.right
            it.top = projection.top
            it.bottom = projection.bottom
        }

        canvas?.drawBitmap(
            bitmapSnapshot,
            srcRect,
            dstRect,
            paint
        )

        return true
    }

    override fun recycle() {
        super.recycle()

        bitmap?.recycle()
        bitmap = null
    }
}
