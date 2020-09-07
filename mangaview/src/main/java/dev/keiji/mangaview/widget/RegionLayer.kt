package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
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

    private fun projectRegionToContentAndGlobal(
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

        handleOnSelectedRegionEvent(x, y) { onSelectedRegionListener, region ->
            return@handleOnSelectedRegionEvent onSelectedRegionListener.onTapRegion(
                pageSnapshot,
                this,
                region,
                tmpSelectedRegionContent,
                tmpSelectedRegionGlobal
            )
        }

        return false
    }

    override fun onDoubleTap(x: Float, y: Float): Boolean {
        super.onDoubleTap(x, y)

        val pageSnapshot = page ?: return false

        handleOnSelectedRegionEvent(x, y) { onSelectedRegionListener, region ->
            return@handleOnSelectedRegionEvent onSelectedRegionListener.onDoubleTapRegion(
                pageSnapshot,
                this,
                region,
                tmpSelectedRegionContent,
                tmpSelectedRegionGlobal
            )
        }

        return false
    }

    override fun onLongTap(mangaView: MangaView, x: Float, y: Float): Boolean {
        super.onLongTap(mangaView, x, y)

        val pageSnapshot = page ?: return false

        handleOnSelectedRegionEvent(x, y) { onSelectedRegionListener, region ->
            return@handleOnSelectedRegionEvent onSelectedRegionListener.onLongTapRegion(
                pageSnapshot,
                this,
                region,
                tmpSelectedRegionContent,
                tmpSelectedRegionGlobal
            )
        }

        return false
    }

    private fun handleOnSelectedRegionEvent(
        x: Float, y: Float,
        fireEvent: (OnSelectedRegionListener, Region) -> Boolean
    ) {
        regionSource.regionList.forEachIndexed { index, region ->
            val path = regionSource.pathList[index]
            path.computeBounds(tmpIntersectBounds, true)

            if (tmpIntersectBounds.contains(x, y)) {
                projectRegionToContentAndGlobal(
                    tmpIntersectBounds,
                    tmpSelectedRegionContent,
                    tmpSelectedRegionGlobal
                )

                onSelectedRegionListenerList.forEach {
                    if (fireEvent(it, region)) {
                        return@forEach
                    }
                }
            }
        }
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
            selectedRegionContent: Rectangle,
            selectedRegionGlobal: Rectangle
        ): Boolean = false

        fun onDoubleTapRegion(
            page: Page,
            layer: RegionLayer,
            region: Region,
            selectedRegionContent: Rectangle,
            selectedRegionGlobal: Rectangle
        ): Boolean = false

        fun onLongTapRegion(
            page: Page,
            layer: RegionLayer,
            region: Region,
            selectedRegionContent: Rectangle,
            selectedRegionGlobal: Rectangle
        ): Boolean = false
    }
}
