package jp.co.c_lis.mangaview.widget

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class HorizontalRtlLayoutManager : LayoutManager() {

    companion object {
        private val TAG = HorizontalRtlLayoutManager::class.java.simpleName
    }

    override val populateHelper: PopulateHelper = HorizontalPopulateHelper()

    override val initialScrollX: Float
        get() = -viewWidth.toFloat()

    override val initialScrollY: Float
        get() = 0.0F

    override fun init() {
    }

    override fun currentPageLayoutIndex(
        viewState: ViewState
    ): Int = abs(viewState.viewport.centerX / viewState.viewWidth).toInt()

    override fun leftPageLayout(viewState: ViewState): PageLayout? {
        val leftIndex = currentPageLayoutIndex(viewState) + 1
        if (leftIndex >= pageLayoutManager.getCount()) {
            return null
        }
        return getPageLayout(leftIndex, viewState)

    }

    override fun rightPageLayout(viewState: ViewState): PageLayout? {
        val rightIndex = currentPageLayoutIndex(viewState) - 1
        if (rightIndex < 0) {
            return null
        }
        return getPageLayout(rightIndex, viewState)
    }

    override fun layout(index: Int, pageLayout: PageLayout, viewState: ViewState): PageLayout {
        val positionLeft = viewState.viewWidth * -(index + 1)

        pageLayout.position.also {
            it.left = positionLeft
            it.right = it.left + viewState.viewWidth
            it.top = 0.0F
            it.bottom = viewState.viewHeight
        }

        return pageLayout
    }

    override fun calcLastVisiblePageLayoutIndex(viewState: ViewState): Int {
        return abs(floor(viewState.viewport.right / viewState.viewWidth)).toInt() - 1
    }

    override fun calcFirstVisiblePageLayoutIndex(viewState: ViewState): Int {
        return abs(ceil(viewState.viewport.right / viewState.viewWidth)).toInt() - 1
    }
}

