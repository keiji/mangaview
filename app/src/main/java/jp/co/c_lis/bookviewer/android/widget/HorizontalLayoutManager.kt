package jp.co.c_lis.bookviewer.android.widget

import android.util.Log
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class HorizontalLayoutManager(
    private val reversed: Boolean = false
) : LayoutManager() {

    companion object {
        private val TAG = HorizontalLayoutManager::class.java.simpleName
    }

    override fun visiblePages(
        viewState: ViewState,
        resultList: ArrayList<Page>
    ): List<Page> {
        val pageIndexLeft = abs(floor(viewState.viewport.right / viewState.viewWidth)).toInt()
        val pageIndexRight = abs(ceil(viewState.viewport.right / viewState.viewWidth)).toInt()

        var startIndex = pageIndexLeft
        var endIndex = pageIndexRight
        if (pageIndexLeft > pageIndexRight) {
            startIndex = pageIndexRight
            endIndex = pageIndexLeft
        }

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
                index + 1
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
}

