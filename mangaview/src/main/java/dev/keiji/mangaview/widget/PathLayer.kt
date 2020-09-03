package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import dev.keiji.mangaview.Log
import kotlin.math.max
import kotlin.math.min

class PathLayer(
    private val pathSource: PathSource
) : ContentLayer(pathSource) {

    companion object {
        private val TAG = PathLayer::class.java.simpleName
    }

    override fun onDraw(
        canvas: Canvas?,
        srcRect: Rect,
        dstRect: RectF,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean {
        Log.d(TAG, "onDraw")
        canvas ?: return false

        val pathList = pathSource.pathList

        val left = min(globalRect.left - viewContext.currentX, 0.0F)
        val top = min(globalRect.top - viewContext.currentY, 0.0F)
        canvas.save()
        canvas.translate(left, top)
        canvas.translate(dstRect.left, dstRect.top)
        canvas.scale(viewContext.currentScale, viewContext.currentScale)
        Log.d(TAG, pathList[0].toString())
        canvas.drawPath(pathList[0], paint)
        canvas.restore()

        return true
    }

    override fun onRecycled() {
        super.onRecycled()

        pathSource.pathList.clear()
    }
}
