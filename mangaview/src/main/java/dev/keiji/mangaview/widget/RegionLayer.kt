package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import dev.keiji.mangaview.BuildConfig
import dev.keiji.mangaview.Rectangle
import dev.keiji.mangaview.Region
import kotlin.math.min

class RegionLayer(
    private val regionSource: RegionSource
) : ContentLayer(regionSource) {

    companion object {
        private val TAG = RegionLayer::class.java.simpleName
    }

    private val onSelectedRegionListenerList = ArrayList<OnSelectedRegionListener>()

    fun addOnSelectRegionListener(onSelectedRegionListener: OnSelectedRegionListener) {
        onSelectedRegionListenerList.add(onSelectedRegionListener)
    }

    fun removeOnSelectRegionListener(onSelectedRegionListener: OnSelectedRegionListener) {
        onSelectedRegionListenerList.remove(onSelectedRegionListener)
    }

    private val matrix = Matrix()

    override fun onDraw(
        canvas: Canvas?,
        page: Page,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean {
        if (!BuildConfig.DEBUG) {
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

        canvas.setMatrix(matrix)

        regionSource.pathList.forEach { path ->
            canvas.drawPath(path, paint)
        }

        canvas.setMatrix(null)

        canvas.restoreToCount(sc)

        return true
    }

    private val tmpIntersectBounds = RectF()
    private val tmpSelectedRegionContent = Rectangle()
    private val tmpSelectedRegionGlobal = Rectangle()

    override fun onLongTap(mangaView: MangaView, x: Float, y: Float): Boolean {
        val pageSnapshot = page ?: return false
        super.onLongTap(mangaView, x, y)

        regionSource.regionList.forEachIndexed { index, region ->
            val path = regionSource.pathList[index]
            path.computeBounds(tmpIntersectBounds, true)

            if (tmpIntersectBounds.contains(x, y)) {
                fireSelectedRegionEvent(pageSnapshot, region, tmpIntersectBounds, path)
            }
        }

        return false
    }

    private fun fireSelectedRegionEvent(page: Page, region: Region, selectedBounds: RectF, path: Path) {
        tmpSelectedRegionContent.copyFrom(selectedBounds)
        tmpSelectedRegionGlobal.copyFrom(selectedBounds)

        tmpSelectedRegionGlobal.also {
            it.left = it.left * baseScale
            it.top = it.top * baseScale
            it.right = it.right * baseScale
            it.bottom = it.bottom * baseScale
        }
        tmpSelectedRegionGlobal
            .offset(globalPosition.left, globalPosition.top)

        onSelectedRegionListenerList.forEach {
            it.onSelectedRegion(page, this, region, tmpSelectedRegionContent, tmpSelectedRegionGlobal)
        }
    }

    override fun onRecycled() {
        super.onRecycled()

        regionSource.recycle()
    }

    interface OnSelectedRegionListener {
        fun onSelectedRegion(
            page: Page,
            layer: RegionLayer,
            region: Region,
            selectedRegionContent: Rectangle,
            selectedRegionGlobal: Rectangle
        )
    }
}
