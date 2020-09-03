package dev.keiji.mangaview.widget

import android.graphics.Bitmap
import dev.keiji.mangaview.TiledSource
import java.util.concurrent.ConcurrentHashMap

abstract class TiledImageSource(
    val tiledSource: TiledSource,
) : ImageSource() {

    val tileList: ArrayList<TiledSource.Tile>
        get() = tiledSource.tileList

    val cachedTiles: Set<TiledSource.Tile>
        get() = cacheBin.keys

    val cacheBin = ConcurrentHashMap<TiledSource.Tile, Bitmap?>()

    override val contentWidth: Float
        get() = tiledSource.sourceWidth

    override val contentHeight: Float
        get() = tiledSource.sourceHeight

    override fun recycle() {
        cacheBin.keys.forEach { tile ->
            recycle(tile)
        }
        cacheBin.clear()
    }

    open fun recycle(tile: TiledSource.Tile) {
        cacheBin[tile]?.recycle()
        cacheBin.remove(tile)
    }

    abstract fun load(tile: TiledSource.Tile): Bitmap?
}
