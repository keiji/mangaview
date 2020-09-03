package dev.keiji.mangaview

import kotlin.jvm.Throws
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

data class TiledSource(
    val sourceWidth: Float,
    val sourceHeight: Float,
    val tileWidth: Float,
    val tileHeight: Float,
    val strideHorizontal: Float,
    val strideVertical: Float,
    val colCount: Int,
    val rowCount: Int,
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
                sourceWidth, sourceHeight,
                tileWidth, tileHeight,
                strideHorizontal, strideVertical,
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
                        Tile(index, x, y, position = Rectangle(left, top, right, bottom))
                    )

                    index++
                }
            }

            return tiledSource
        }
    }

    data class Tile(
        val index: Int,
        val x: Int,
        val y: Int,
        val position: Rectangle = Rectangle()
    )
}
