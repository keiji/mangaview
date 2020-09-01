package dev.keiji.mangaview.widget

class DoubleTapZoomHelper(
    private val maxScale: Float = DEFAULT_DOUBLE_TAP_SCALE
) : OnDoubleTapListener {

    companion object {
        private val TAG = DoubleTapZoomHelper::class.java.simpleName

        private const val DEFAULT_DOUBLE_TAP_SCALE = 4.0F
    }

    fun setup(mangaView: MangaView) {
        mangaView.addOnDoubleTapListener(this)
    }

    override fun onDoubleTap(mangaView: MangaView, x: Float, y: Float): Boolean {
        return doubleTapZoom(mangaView, x, y)
    }

    private fun doubleTapZoom(mangaView: MangaView, x: Float, y: Float): Boolean {
        val viewContext = mangaView.viewContext

        if (viewContext.currentScale >= maxScale) {
            mangaView.scale(viewContext.minScale, x, y, smoothScale = true)
        } else {
            mangaView.scale(maxScale, x, y, smoothScale = true)
        }

        return true
    }
}
