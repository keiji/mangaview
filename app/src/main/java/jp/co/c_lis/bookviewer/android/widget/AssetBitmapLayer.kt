package jp.co.c_lis.bookviewer.android.widget

import android.content.res.AssetManager
import android.graphics.*
import android.util.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt

class AssetBitmapLayer(
    private val assetManager: AssetManager,
    private val fileName: String
) : ContentLayer() {

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

    override suspend fun prepareContent(viewState: ViewState, pageRect: Rectangle) {
        bitmap = assetManager.open(fileName).use {
            BitmapFactory.decodeStream(it)
        }
    }

    private fun scaleRect(
        from: Rectangle,
        scaleHorizontal: Float,
        scaleVertical: Float,
        out: Rect
    ): Rect {
        return out.also {
            out.left = (from.left * scaleHorizontal).roundToInt()
            out.top = (from.top * scaleVertical).roundToInt()
            out.right = (from.right * scaleHorizontal).roundToInt()
            out.bottom = (from.bottom * scaleVertical).roundToInt()
        }
    }

    private val dstRect = RectF()

    private val srcAbsoluteRect = Rect()

    override fun onDraw(
        canvas: Canvas?,
        viewState: ViewState,
        page: Page,
        paint: Paint,
        coroutineScope: CoroutineScope
    ): Boolean {

        Log.d(TAG, page.position.toString())
        Log.d(TAG, viewState.viewport.toString())

        val bitmapSnapshot = bitmap ?: return false

        dstRect.also {
            it.left = page.position.left * viewState.viewWidth
            it.right = page.position.right * viewState.viewWidth
            it.top = page.position.top * viewState.viewHeight
            it.bottom = page.position.bottom * viewState.viewHeight
        }

        scaleRect(
            page.position,
            contentWidth, contentHeight,
            srcAbsoluteRect
        )

        Log.d(
            TAG,
            "dstRect.left:${dstRect.left}, " +
                    "top:${dstRect.top}, " +
                    "right:${dstRect.right}, " +
                    "bottom:${dstRect.bottom}"
        )
        Log.d(
            TAG,
            "srcAbsoluteRect.left:${srcAbsoluteRect.left}, " +
                    "top:${srcAbsoluteRect.top}, " +
                    "right:${srcAbsoluteRect.right}, " +
                    "bottom:${srcAbsoluteRect.bottom}"
        )

        canvas?.drawBitmap(
            bitmapSnapshot,
            srcAbsoluteRect,
            dstRect,
            paint
        )

        return true
    }
}
