package jp.co.c_lis.bookviewer.android.widget

import jp.co.c_lis.bookviewer.android.Rectangle
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class HorizontalLayoutManager(
    private val reversed: Boolean = false
) : LayoutManager() {

    companion object {
        private val TAG = HorizontalLayoutManager::class.java.simpleName

        private const val VELOCITY_RATIO_THRESHOLD = 0.25F
    }

    override fun currentPageIndex(
        viewState: ViewState
    ): Int = abs(viewState.viewport.center / viewState.viewWidth).toInt()

    override fun currentPageRect(viewState: ViewState): Rectangle {
        return pageList[currentPageIndex(viewState)].position
    }

    override fun getPageRect(pageIndex: Int): Rectangle {
        return pageList[pageIndex].position
    }

    override fun visiblePages(
        viewState: ViewState,
        resultList: ArrayList<Page>,
        offsetScreenPageLimit: Int
    ): List<Page> {
        val pageIndexLeft = if (reversed) {
            abs(floor(viewState.viewport.right / viewState.viewWidth)).toInt()
        } else {
            abs(floor(viewState.viewport.left / viewState.viewWidth)).toInt()
        }
        val pageIndexRight = if (reversed) {
            abs(ceil(viewState.viewport.right / viewState.viewWidth)).toInt()
        } else {
            abs(ceil(viewState.viewport.left / viewState.viewWidth)).toInt()
        }

        var startIndex = pageIndexLeft
        var endIndex = pageIndexRight
        if (pageIndexLeft > pageIndexRight) {
            startIndex = pageIndexRight
            endIndex = pageIndexLeft
        }

        startIndex -= offsetScreenPageLimit
        endIndex += offsetScreenPageLimit

        if (startIndex < 0) {
            startIndex = 0
        }
        if (endIndex >= pageList.size) {
            endIndex = pageList.size - 1
        }

        resultList.clear()

        (startIndex..endIndex).forEach { index ->
            resultList.add(pageList[index])
        }

        return resultList
    }

    override fun layout(viewState: ViewState) {

        // layout pages
        for (index in pageList.indices) {
            val page = pageList[index]

            val positionLeft = viewState.viewWidth * if (!reversed) {
                index
            } else {
                -(index + 1)
            }

            page.position.also {
                it.left = positionLeft
                it.right = it.left + viewState.viewWidth
                it.top = 0.0F
                it.bottom = viewState.viewHeight
            }
        }

        viewState.scrollableArea.also { area ->
            area.left = pageList.minBy { it.position.left }?.position?.left ?: 0.0F
            area.right = pageList.maxBy { it.position.right }?.position?.right ?: 0.0F
            area.top = pageList.minBy { it.position.top }?.position?.top ?: 0.0F
            area.bottom = pageList.maxBy { it.position.bottom }?.position?.bottom ?: 0.0F
        }

        viewState.offset(-viewState.viewWidth, 0.0F)
    }

    override fun nextPageRect(
        viewState: ViewState,
        velocityRatioX: Float,
        velocityRatioY: Float
    ): Rectangle? {
        if (abs(velocityRatioX) < VELOCITY_RATIO_THRESHOLD) {
            return null
        }

        val currentPageIndex = currentPageIndex(viewState)

        var nextPageIndex = when {
            reversed && velocityRatioX > 0 -> currentPageIndex + 1
            reversed && velocityRatioX < 0 -> currentPageIndex - 1
            !reversed && velocityRatioX > 0 -> currentPageIndex - 1
            !reversed && velocityRatioX < 0 -> currentPageIndex + 1
            else -> null
        }

        nextPageIndex ?: return null

        if (nextPageIndex < 0) {
            nextPageIndex = 0
        }
        if (nextPageIndex >= pageList.size) {
            nextPageIndex = pageList.size - 1
        }

        return getPageRect(nextPageIndex)
    }
}

