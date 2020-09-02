package dev.keiji.mangaview.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import dev.keiji.mangaview.BuildConfig
import dev.keiji.mangaview.Rectangle
import kotlin.jvm.Throws
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

data class TiledSource(
    val sourceWidth: Float,
    val sourceHeight: Float,
    var colCount: Int,
    var rowCount: Int,
    val tileList: ArrayList<Tile> = ArrayList()
) {
    companion object {
        private val TAG = TiledSource::class.java.simpleName

        @Throws(IllegalArgumentException::class)
        fun build(
            sourceWidth: Float, sourceHeight: Float,
            tileWidth: Float, tileHeight: Float,
            strideHorizontal: Float = tileWidth, strideVertical: Float = tileHeight
        ): TiledSource {
            if (sourceWidth < tileWidth) {
                throw IllegalArgumentException("tileWidth must not be greater than sourceWidth.")
            }
            if (sourceHeight < tileHeight) {
                throw IllegalArgumentException("tileHeight must not be greater than sourceHeight.")
            }
            if (strideHorizontal > tileWidth) {
                throw IllegalArgumentException("strideHorizontal must not be greater than tileWidth.")
            }
            if (strideVertical > tileHeight) {
                throw IllegalArgumentException("strideVertical must not be greater than tileHeight.")
            }

            val tiledSource = TiledSource(
                sourceWidth,
                sourceHeight,
                colCount = ceil(sourceWidth / strideHorizontal).roundToInt(),
                rowCount = ceil(sourceHeight / strideVertical).roundToInt()
            )

            var index = 0

            for (y in 0 until tiledSource.rowCount) {
                for (x in 0 until tiledSource.colCount) {
                    val left = x * strideHorizontal
                    var right = left + tileWidth
                    val top = y * strideVertical
                    var bottom = top + tileHeight

                    right = min(right, sourceWidth)
                    bottom = min(bottom, sourceHeight)

                    tiledSource.tileList.add(
                        Tile(index, position = Rectangle(left, top, right, bottom))
                    )

                    index++
                }
            }

            return tiledSource
        }
    }

    data class Tile(
        val index: Int,
        val position: Rectangle = Rectangle()
    )
}

class TiledBitmapLayer(
    private val tiledImageSource: TiledImageSource,
    private val scaleThreshold: Float = DEFAULT_SCALE_SHOW_TILE_THRESHOLD,
) : ContentLayer(tiledImageSource) {

    companion object {
        private val TAG = TiledBitmapLayer::class.java.simpleName

        private const val DEFAULT_SCALE_SHOW_TILE_THRESHOLD = 3.0F
    }

    private val tmpTilePosition = Rectangle()

    private val displayTileList = ArrayList<TiledSource.Tile>()
    private val recycleBin = ArrayList<TiledSource.Tile>()

    override fun onDraw(
        canvas: Canvas?,
        srcRect: Rect,
        dstRect: RectF,
        viewContext: ViewContext,
        paint: Paint
    ): Boolean {
        val pageSnapshot = page ?: return false

        recycleBin.addAll(tiledImageSource.cachedTiles)

        displayTileList.clear()

        tiledImageSource.tileList.forEach { tile ->
            if (tile.position.intersect(contentSrc)) {
                recycleBin.remove(tile)
                displayTileList.add(tile)
            }
        }

        val allTilesShown = if (viewContext.currentScale >= scaleThreshold) {
            drawTiles(canvas, pageSnapshot, paint)
        } else {
            true
        }

        recycleBin.forEach {
            tiledImageSource.recycle(it)
        }
        recycleBin.clear()

        return allTilesShown
    }

    private fun drawTiles(
        canvas: Canvas?,
        pageSnapshot: Page,
        paint: Paint
    ): Boolean {
        var allTilesShown = true

        displayTileList.forEach { tile ->
            val tiledBitmap = tiledImageSource.load(tile)
            if (tiledBitmap == null) {
                allTilesShown = false
                return@forEach
            }

            tmpTilePosition
                .copyFrom(tile.position)
                .relativeBy(contentSrc)

            project(
                tmpTilePosition,
                contentSrc,
                pageSnapshot.displayProjection,
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

        return allTilesShown
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
        tiledImageSource.recycle()

        super.onRecycled()
    }
}
