package dev.keiji.mangaview

import dev.keiji.mangaview.widget.MangaView

/**
 * Helper class that behavior when the user touched edge on the view.
 *
 * Implementation of the MangaView.OnTapListener supporting page transition in either vertical or horizontal orientation.
 */
class EdgeNavigationHelper : MangaView.OnTapListener {

    companion object {
        private const val DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_HORIZONTAL = 0.2F
        private const val DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_VERTICAL = 0.2F
    }

    private val tapEdgeScrollThresholdLeft = DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_HORIZONTAL
    private val tapEdgeScrollThresholdRight = 1.0F - DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_HORIZONTAL
    private val tapEdgeScrollThresholdTop = DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_VERTICAL
    private val tapEdgeScrollThresholdBottom = 1.0F - DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_VERTICAL

    /**
     * Attaches the EdgeNavigationHelper to the provided MangaView.
     *
     * @param mangaView The MangaView instance to which you want to add this helper.
     */
    fun attachToMangaView(mangaView: MangaView) {
        mangaView.addOnTapListener(this)
    }

    /**
     * Detaches the EdgeNavigationHelper from the provided MangaView.
     *
     * @param mangaView The MangaView instance to which you want to remove this helper.
     */
    fun detachToMangaView(mangaView: MangaView) {
        mangaView.removeOnTapListener(this)
    }

    override fun onTap(mangaView: MangaView, viewX: Float, viewY: Float): Boolean {
        val viewContext = mangaView.viewContext

        val tapEdgeLeft = viewContext.viewWidth * tapEdgeScrollThresholdLeft
        val tapEdgeRight = viewContext.viewWidth * tapEdgeScrollThresholdRight
        val tapEdgeTop = viewContext.viewHeight * tapEdgeScrollThresholdTop
        val tapEdgeBottom = viewContext.viewHeight * tapEdgeScrollThresholdBottom

        val currentScrollAreaSnapshot = mangaView.currentPageLayout?.scrollArea ?: return false
        if (!viewContext.viewport.contains(currentScrollAreaSnapshot)) {
            return false
        }

        if (viewX < tapEdgeLeft
            && (toLeftPage(mangaView) || handleReadCompleteEvent(mangaView))
        ) {
            return true
        }

        if (viewX > tapEdgeRight
            && (toRightPage(mangaView) || handleReadCompleteEvent(mangaView))
        ) {
            return true
        }

        if (viewY < tapEdgeTop
            && (toTopPage(mangaView)) || handleReadCompleteEvent(mangaView)
        ) {
            return true
        }

        if (viewY > tapEdgeBottom
            && (toBottomPage(mangaView) || handleReadCompleteEvent(mangaView))
        ) {
            return true
        }

        return false
    }

    private fun handleReadCompleteEvent(
        mangaView: MangaView
    ): Boolean {
        val viewContext = mangaView.viewContext
        val layoutManager = mangaView.layoutManager ?: return false

        val currentPageLayout = layoutManager.currentPageLayout(viewContext)
        val lastPageLayout = layoutManager.lastPageLayout(viewContext)

        return if (currentPageLayout == lastPageLayout) {
            mangaView.fireEventReadComplete()
        } else {
            false
        }
    }

    private fun toLeftPage(
        mangaView: MangaView
    ): Boolean {
        val viewContext = mangaView.viewContext
        val layoutManager = mangaView.layoutManager ?: return false

        val index = layoutManager.leftPageLayout(viewContext)
            ?.keyPage?.index ?: return false
        mangaView.showPage(index, smoothScroll = true)
        return true
    }

    private fun toRightPage(
        mangaView: MangaView
    ): Boolean {
        val viewContext = mangaView.viewContext
        val layoutManager = mangaView.layoutManager ?: return false

        val index = layoutManager.rightPageLayout(viewContext)
            ?.keyPage?.index ?: return false
        mangaView.showPage(index, smoothScroll = true)
        return true
    }

    private fun toTopPage(
        mangaView: MangaView
    ): Boolean {
        val viewContext = mangaView.viewContext
        val layoutManager = mangaView.layoutManager ?: return false

        val index = layoutManager.topPageLayout(viewContext)
            ?.keyPage?.index ?: return false
        mangaView.showPage(index, smoothScroll = true)
        return false
    }

    private fun toBottomPage(
        mangaView: MangaView
    ): Boolean {
        val viewContext = mangaView.viewContext
        val layoutManager = mangaView.layoutManager ?: return false

        val index = layoutManager.bottomPageLayout(viewContext)
            ?.keyPage?.index ?: return false
        mangaView.showPage(index, smoothScroll = true)
        return false
    }
}
