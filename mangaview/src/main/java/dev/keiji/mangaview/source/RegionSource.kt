package dev.keiji.mangaview.source

import android.graphics.Path
import dev.keiji.mangaview.Region

abstract class RegionSource : ContentSource() {

    abstract val regionList: ArrayList<Region>

    private val cachedPathList = ArrayList<Path>()

    val pathList: ArrayList<Path>
        get() {
            if (regionList.size == cachedPathList.size) {
                return cachedPathList
            }

            cachedPathList.clear()
            regionList.forEach { region ->
                cachedPathList.add(region.toPath(contentWidth, contentHeight))
            }
            return cachedPathList
        }

    fun getPath(region: Region) = pathList[regionList.indexOf(region)]
}
