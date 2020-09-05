package dev.keiji.mangaview.widget

import android.graphics.Path
import dev.keiji.mangaview.Region

abstract class RegionSource : ContentSource() {

    abstract val regionList: ArrayList<Region>

    val pathList: ArrayList<Path> by lazy {
        val list = ArrayList<Path>()
        regionList.forEach { region ->
            list.add(region.toPath(contentWidth, contentHeight))
        }
        list
    }
}
