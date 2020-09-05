package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import dev.keiji.mangaview.BuildConfig
import dev.keiji.mangaview.Rectangle
import kotlin.math.min

class PathLayer(
    private val pathSource: PathSource
) : ContentLayer(pathSource) {

    companion object {
        private val TAG = PathLayer::class.java.simpleName
    }

    private val matrix = Matrix()

    override fun onDraw(
        canvas: Canvas?,
        page: Page,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean {
        if (BuildConfig.DEBUG) {
            return true
        }

        canvas ?: return false

        val sc = canvas.save()

        val left = min(globalPosition.left - viewContext.currentX, page.displayProjection.left)
        val top = min(globalPosition.top - viewContext.currentY, page.displayProjection.top)

        val scaledLeft = left * viewContext.currentScale
        val scaledTop = top * viewContext.currentScale

        val scaleHorizontal = viewContext.currentScale * baseScale
        val scaleVertical = viewContext.currentScale * baseScale

        matrix.setScale(scaleHorizontal, scaleVertical)
        matrix.postTranslate(scaledLeft, scaledTop)

        pathSource.pathList.forEach { path ->

            if (path == selectedPath) {
                canvas.setMatrix(matrix)
                canvas.drawPath(path, paint)
                canvas.setMatrix(null)
            }
        }

        canvas.restoreToCount(sc)

        return true
    }

    private val tmpIntersectBounds = RectF()
    private val tmpSelectedBounds = Rectangle()

    private var selectedPath: Path? = null

    override fun onLongTap(mangaView: MangaView, x: Float, y: Float): Boolean {
        super.onLongTap(mangaView, x, y)

        selectedPath = null

        pathSource.pathList.forEach { path ->
            path.computeBounds(tmpIntersectBounds, true)

            if (tmpIntersectBounds.contains(x, y)) {
                selectedPath = path
                tmpSelectedBounds.copyFrom(tmpIntersectBounds)
                return@forEach
            }
        }

        if (selectedPath != null) {
            tmpSelectedBounds.also {
                it.left = it.left * baseScale
                it.top = it.top * baseScale
                it.right = it.right * baseScale
                it.bottom = it.bottom * baseScale
            }
            tmpSelectedBounds
                .offset(globalPosition.left, globalPosition.top)

            mangaView.focus(tmpSelectedBounds)
        }

        return false
    }

    override fun onRecycled() {
        super.onRecycled()

        pathSource.recycle()
    }
}
