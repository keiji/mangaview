package dev.keiji.mangaview

import dev.keiji.mangaview.widget.MangaView

/**
 * Helper class that behavior when the user double tapped on the view.
 *
 * Implementation of the MangaView.OnDoubleTapListener supporting page zooming.
 *
 * @constructor
 */
class DoubleTapZoomHelper(
    private val maxScale: Float = DEFAULT_DOUBLE_TAP_SCALE
) : MangaView.OnDoubleTapListener {

    companion object {
        private val TAG = DoubleTapZoomHelper::class.java.simpleName

        private const val DELTA = 0.001F
        private const val DEFAULT_DOUBLE_TAP_SCALE = 4.0F
    }

    /**
     * Attaches the DoubleTapZoomHelper to the provided MangaView.
     *
     * @param mangaView The MangaView instance to which you want to add this helper.
     */
    fun attachToMangaView(mangaView: MangaView) {
        mangaView.addOnDoubleTapListener(this)
    }

    /**
     * Detaches the DoubleTapZoomHelper from the provided MangaView.
     *
     * @param mangaView The MangaView instance to which you want to remove this helper.
     */
    fun detachToMangaView(mangaView: MangaView) {
        mangaView.removeOnDoubleTapListener(this)
    }

    override fun onDoubleTap(mangaView: MangaView, viewX: Float, viewY: Float): Boolean {
        return doubleTapZoom(mangaView, viewX, viewY)
    }

    private fun doubleTapZoom(mangaView: MangaView, x: Float, y: Float): Boolean {
        val viewContext = mangaView.viewContext

        if ((maxScale - viewContext.currentScale) < DELTA) {
            mangaView.scale(viewContext.minScale, x, y, smoothScale = true)
        } else {
            mangaView.scale(maxScale, x, y, smoothScale = true)
        }

        return true
    }
}
