package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.VisibleForTesting
import dev.keiji.mangaview.BuildConfig
import dev.keiji.mangaview.Rectangle
import dev.keiji.mangaview.TiledSource
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class TiledBitmapLayer(
    private val tiledBitmapSource: TiledBitmapSource,
    private val scaleThreshold: Float = DEFAULT_SCALE_SHOW_TILE_THRESHOLD,
    private val offscreenTileLimit: Int = 0
) : ContentLayer(tiledBitmapSource) {

    companion object {
        private val TAG = TiledBitmapLayer::class.java.simpleName

        private const val DEFAULT_SCALE_SHOW_TILE_THRESHOLD = 3.0F
    }

    @VisibleForTesting
    val srcRect = Rect()

    @VisibleForTesting
    val dstRect = RectF()

    private val tmpTilePosition = Rectangle()

    private val displayTileList = ArrayList<TiledSource.Tile>()
    private val recycleBin = ArrayList<TiledSource.Tile>()

    override fun onDraw(
        canvas: Canvas?,
        page: Page,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean {

        recycleBin.addAll(tiledBitmapSource.cachedTiles)

        val allTilesShown = if (viewContext.currentScale >= scaleThreshold) {
            searchVisibleTiles(
                tiledBitmapSource.tiledSource,
                tiledBitmapSource.tileList,
                contentSrc,
                displayTileList,
                offscreenTileLimit = offscreenTileLimit
            )
            drawTiles(canvas, page.displayProjection, displayTileList, paint)
        } else {
            true
        }

        recycleBin.forEach {
            if (!displayTileList.contains(it)) {
                tiledBitmapSource.recycle(it)
            }
        }
        recycleBin.clear()

        return allTilesShown
    }

    private fun searchVisibleTiles(
        tiledSource: TiledSource,
        tileList: ArrayList<TiledSource.Tile>,
        contentSrc: Rectangle,
        displayTileList: ArrayList<TiledSource.Tile>,
        offscreenTileLimit: Int
    ) {
        displayTileList.clear()

        val centerX = (contentSrc.centerX / tiledSource.strideHorizontal).roundToInt()
        val centerY = (contentSrc.centerY / tiledSource.strideVertical).roundToInt()

        var leftX = centerX
        var rightX = centerX
        var topY = centerY
        var bottomY = centerY

        // search visible area to left
        for (x in (1..centerX)) {
            val baseX = centerX - x
            val index = centerY * tiledSource.colCount + baseX
            if (index >= tileList.size) {
                break
            }

            if (tileList[index].position.intersect(contentSrc)) {
                leftX = baseX
            } else {
                break
            }
        }

        // search visible area to right
        for (x in (centerX + 1 until tiledSource.colCount)) {
            val index = centerY * tiledSource.colCount + x
            if (index >= tileList.size) {
                break
            }

            if (tileList[index].position.intersect(contentSrc)) {
                rightX = x
            } else {
                break
            }
        }

        // search visible area to top
        for (y in (1..centerY)) {
            val baseY = centerY - y
            val index = baseY * tiledSource.colCount + centerX
            if (index >= tileList.size) {
                break
            }

            if (tileList[index].position.intersect(contentSrc)) {
                topY = baseY
            } else {
                break
            }
        }

        // search visible area to bottom
        for (y in (centerY + 1 until tiledSource.rowCount)) {
            val index = y * tiledSource.colCount + centerX
            if (index >= tileList.size) {
                break
            }

            if (tileList[index].position.intersect(contentSrc)) {
                bottomY = y
            } else {
                break
            }
        }

        // add visible area
        leftX -= offscreenTileLimit
        rightX += offscreenTileLimit
        topY -= offscreenTileLimit
        bottomY += offscreenTileLimit

        leftX = max(leftX, 0)
        rightX = min(rightX, tiledSource.colCount - 1)
        topY = max(topY, 0)
        bottomY = min(bottomY, tiledSource.rowCount - 1)

        for (y in (topY..bottomY)) {
            for (x in (leftX..rightX)) {
                val index = y * tiledSource.colCount + x
                displayTileList.add(tileList[index])
            }
        }
    }

    private fun drawTiles(
        canvas: Canvas?,
        displayProjection: Rectangle,
        displayTileList: ArrayList<TiledSource.Tile>,
        paint: Paint
    ): Boolean {
        var allTilesAreShown = true

        displayTileList.forEach { tile ->
            val tiledBitmap = tiledBitmapSource.load(tile)
            if (tiledBitmap == null) {
                allTilesAreShown = false
                return@forEach
            }

            tmpTilePosition
                .copyFrom(tile.position)
                .relativeBy(contentSrc)

            project(
                tmpTilePosition,
                contentSrc,
                displayProjection,
                tmpTilePosition
            )
            tmpTilePosition.copyTo(dstRect)

            srcRect.set(
                0,
                0,
                tiledBitmap.width,
                tiledBitmap.height
            )

            canvas?.drawBitmap(tiledBitmap, srcRect, dstRect, paint)

            if (BuildConfig.DEBUG) {
                canvas?.drawRect(dstRect, paint)
            }
        }

        return allTilesAreShown
    }

    private fun project(
        tilePosition: Rectangle,
        contentSrc: Rectangle,
        displayProjection: Rectangle,
        result: Rectangle
    ) {
        val widthRatio = displayProjection.width / contentSrc.width
        val heightRatio = displayProjection.height / contentSrc.height

        result.left = tilePosition.left * widthRatio
        result.top = tilePosition.top * heightRatio
        result.right = tilePosition.right * widthRatio
        result.bottom = tilePosition.bottom * heightRatio
        result.offset(displayProjection.left, displayProjection.top)
    }

    override fun onRecycled() {
        tiledBitmapSource.recycle()

        super.onRecycled()
    }
}
