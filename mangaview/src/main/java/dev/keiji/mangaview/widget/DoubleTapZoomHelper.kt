package dev.keiji.mangaview.widget

import kotlin.math.max

class DoubleTapZoomHelper : OnDoubleTapListener {

    companion object {
        private val TAG = DoubleTapZoomHelper::class.java.simpleName

        private const val DOUBLE_TAP_ZOOM_MAX_SCALE = 4.0F
    }

    fun setup(mangaView: MangaView) {
        mangaView.addOnDoubleTapListener(this)
    }

    override fun onDoubleTap(mangaView: MangaView, x: Float, y: Float): Boolean {
        return doubleTapZoom(mangaView, x, y)
    }

    private fun doubleTapZoom(mangaView: MangaView, x: Float, y: Float): Boolean {
        val viewContext = mangaView.viewContext
        val currentScrollArea = mangaView.currentPageLayout?.scrollArea ?: return false

        val scale2 = max(
            viewContext.viewWidth / currentScrollArea.width,
            viewContext.viewHeight / currentScrollArea.height
        )

        if (viewContext.currentScale < scale2) {
            mangaView.scale(scale2, x, y, smoothScale = true)
        } else if (viewContext.currentScale >= DOUBLE_TAP_ZOOM_MAX_SCALE) {
            mangaView.scale(viewContext.minScale, x, y, smoothScale = true)
        } else {
            mangaView.scale(DOUBLE_TAP_ZOOM_MAX_SCALE, x, y, smoothScale = true)
        }

        return true
    }
}
