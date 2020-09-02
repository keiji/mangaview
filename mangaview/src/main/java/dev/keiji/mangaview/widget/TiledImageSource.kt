package dev.keiji.mangaview.widget

import android.graphics.Bitmap

abstract class TiledImageSource(
    private val tiledSource: TiledSource,
) : ImageSource() {

    val tileList: ArrayList<TiledSource.Tile>
        get() = tiledSource.tileList

    val cachedTiles: Set<TiledSource.Tile>
        get() = cacheBin.keys

    val cacheBin = HashMap<TiledSource.Tile, Bitmap?>()

    override val contentWidth: Float
        get() = tiledSource.sourceWidth

    override val contentHeight: Float
        get() = tiledSource.sourceHeight

    override fun recycle() {
        synchronized(cacheBin) {
            cacheBin.keys.forEach { tile ->
                cacheBin[tile]?.recycle()
            }
            cacheBin.clear()
        }
    }

    fun recycle(tile: TiledSource.Tile) {
        cacheBin[tile]?.recycle()
        cacheBin.remove(tile)
    }

    abstract fun load(tile: TiledSource.Tile): Bitmap?
}
