package dev.keiji.mangaview.widget

import android.graphics.Bitmap

abstract class BitmapSource : ContentSource() {

    abstract val bitmap: Bitmap?

    override val contentWidth: Float
        get() = bitmap?.width?.toFloat() ?: 0.0F

    override val contentHeight: Float
        get() = bitmap?.height?.toFloat() ?: 0.0F

    override fun recycle() {
        bitmap?.recycle()
    }
}
