package dev.keiji.mangaview

import android.graphics.Path
import android.graphics.PointF

data class Region(
    val categoryId: Int = 0,
    val label: Int = 0,
    val isNormalized: Boolean = true,
    val pointList: ArrayList<PointF> = ArrayList()
) {

    fun addPoint(point: PointF) {
        pointList.add(point)
    }

    fun toPath(parentWidth: Float, parentHeight: Float): Path {
        return Path().also { path ->
            var firstFlg = true

            pointList.forEach { point ->
                val x = if (isNormalized) {
                    point.x * parentWidth
                } else {
                    point.x
                }
                val y = if (isNormalized) {
                    point.y * parentHeight
                } else {
                    point.y
                }

                if (firstFlg) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                firstFlg = false
            }
            path.close()
        }
    }
}

