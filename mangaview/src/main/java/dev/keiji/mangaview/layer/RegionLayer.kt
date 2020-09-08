package dev.keiji.mangaview.layer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import dev.keiji.mangaview.BuildConfig
import dev.keiji.mangaview.Rectangle
import dev.keiji.mangaview.Region
import dev.keiji.mangaview.source.RegionSource
import dev.keiji.mangaview.widget.Page
import dev.keiji.mangaview.widget.ViewContext
import kotlin.math.min
import kotlin.math.sqrt

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

        // Projection view coordinate
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

    fun projectionContentAndGlobal(
        intersectBounds: RectF,
        selectedRegionContent: Rectangle,
        selectedRegionGlobal: Rectangle
    ) {
        selectedRegionContent.copyFrom(intersectBounds)
        selectedRegionGlobal.copyFrom(intersectBounds)

        selectedRegionGlobal.also {
            it.left = it.left * baseScale
            it.top = it.top * baseScale
            it.right = it.right * baseScale
            it.bottom = it.bottom * baseScale
        }
        selectedRegionGlobal
            .offset(globalPosition.left, globalPosition.top)
    }

    override fun onTap(x: Float, y: Float): Boolean {
        super.onTap(x, y)

        val pageSnapshot = page ?: return false

        return handleOnSelectedRegionEvent(x, y) { onSelectedRegionListener, region, bounds ->
            return@handleOnSelectedRegionEvent onSelectedRegionListener.onTapRegion(
                pageSnapshot,
                this,
                region,
                bounds
            )
        }
    }

    override fun onDoubleTap(x: Float, y: Float): Boolean {
        super.onDoubleTap(x, y)

        val pageSnapshot = page ?: return false

        return handleOnSelectedRegionEvent(x, y) { onSelectedRegionListener, region, bounds ->
            return@handleOnSelectedRegionEvent onSelectedRegionListener.onDoubleTapRegion(
                pageSnapshot,
                this,
                region,
                bounds
            )
        }
    }

    override fun onLongTap(x: Float, y: Float): Boolean {
        super.onLongTap(x, y)

        val pageSnapshot = page ?: return false

        return handleOnSelectedRegionEvent(x, y) { onSelectedRegionListener, region, bounds ->
            return@handleOnSelectedRegionEvent onSelectedRegionListener.onLongTapRegion(
                pageSnapshot,
                this,
                region,
                bounds
            )
        }
    }

    private val tmpIntersectRegionList = ArrayList<Region>()

    private fun handleOnSelectedRegionEvent(
        x: Float, y: Float,
        fireEvent: (OnSelectedRegionListener, Region, RectF) -> Boolean
    ): Boolean {
        tmpIntersectRegionList.clear()

        regionSource.regionList.forEachIndexed { index, region ->
            val path = regionSource.pathList[index]
            path.computeBounds(tmpIntersectBounds, true)

            if (tmpIntersectBounds.contains(x, y)) {
                tmpIntersectRegionList.add(region)
            }
        }

        val topIntersectRegion = selectNearestRegion(tmpIntersectRegionList, x, y) ?: return false
        regionSource.getPath(topIntersectRegion).computeBounds(tmpIntersectBounds, true)

        var consumed = false

        onSelectedRegionListenerList.forEach {
            if (fireEvent(it, topIntersectRegion, tmpIntersectBounds)) {
                consumed = true
                return@forEach
            }
        }

        return consumed
    }

    private fun selectNearestRegion(
        intersectRegionList: ArrayList<Region>,
        x: Float,
        y: Float,
    ): Region? {
        if (intersectRegionList.isEmpty()) {
            return null
        }
        if (intersectRegionList.size == 1) {
            return intersectRegionList.first()
        }

        intersectRegionList.sortBy { calcScore(it, x, y) }

        return intersectRegionList.first()
    }

    private fun calcScore(region: Region, x: Float, y: Float): Float {
        val diffHorizontal = tmpIntersectBounds.centerX() - x
        val diffVertical = tmpIntersectBounds.centerY() - y

        return sqrt((diffHorizontal * diffHorizontal) + (diffVertical * diffVertical))
    }

    override fun onRecycled() {
        super.onRecycled()

        regionSource.recycle()
    }

    interface OnSelectedRegionListener {
        fun onTapRegion(
            page: Page,
            layer: RegionLayer,
            region: Region,
            bounds: RectF
        ): Boolean = false

        fun onDoubleTapRegion(
            page: Page,
            layer: RegionLayer,
            region: Region,
            bounds: RectF
        ): Boolean = false

        fun onLongTapRegion(
            page: Page,
            layer: RegionLayer,
            region: Region,
            bounds: RectF
        ): Boolean = false
    }
}
