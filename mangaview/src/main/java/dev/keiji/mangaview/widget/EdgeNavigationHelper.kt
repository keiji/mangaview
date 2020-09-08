package dev.keiji.mangaview.widget

class EdgeNavigationHelper : MangaView.OnTapListener {

    companion object {
        private const val DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_HORIZONTAL = 0.2F
        private const val DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_VERTICAL = 0.2F
    }

    private val tapEdgeScrollThresholdLeft = DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_HORIZONTAL
    private val tapEdgeScrollThresholdRight = 1.0F - DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_HORIZONTAL
    private val tapEdgeScrollThresholdTop = DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_VERTICAL
    private val tapEdgeScrollThresholdBottom = 1.0F - DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_VERTICAL

    fun setup(mangaView: MangaView) {
        mangaView.addOnTapListener(this)
    }

    override fun onTap(mangaView: MangaView, x: Float, y: Float): Boolean {
        val viewContext = mangaView.viewContext

        val tapEdgeLeft = viewContext.viewWidth * tapEdgeScrollThresholdLeft
        val tapEdgeRight = viewContext.viewWidth * tapEdgeScrollThresholdRight
        val tapEdgeTop = viewContext.viewHeight * tapEdgeScrollThresholdTop
        val tapEdgeBottom = viewContext.viewHeight * tapEdgeScrollThresholdBottom

        val currentScrollAreaSnapshot = mangaView.currentPageLayout?.scrollArea ?: return false
        if (!viewContext.viewport.contains(currentScrollAreaSnapshot)) {
            return false
        }

        if (x < tapEdgeLeft
            && (toLeftPage(mangaView) || handleReadCompleteEvent(mangaView))
        ) {
            return true
        }

        if (x > tapEdgeRight
            && (toRightPage(mangaView) || handleReadCompleteEvent(mangaView))
        ) {
            return true
        }

        if (y < tapEdgeTop
            && (toTopPage(mangaView)) || handleReadCompleteEvent(mangaView)
        ) {
            return true
        }

        if (y > tapEdgeBottom
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
