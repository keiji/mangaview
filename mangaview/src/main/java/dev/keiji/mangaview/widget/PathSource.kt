package dev.keiji.mangaview.widget

import android.graphics.Path

abstract class PathSource : ContentSource() {

    val pathList = ArrayList<Path>()
}
