package jp.co.c_lis.mangaview.android.widget

import jp.co.c_lis.mangaview.android.Log
import jp.co.c_lis.mangaview.android.Rectangle
import kotlin.math.roundToInt

class VerticalPopulateHelper : PopulateHelper() {

    companion object {
        private val TAG = VerticalPopulateHelper::class.java.simpleName
    }

    private val shouldPopulateVertical = fun(rect: Rectangle?): Boolean {
        rect ?: return false
        val diff = viewState.viewport.height - rect.height
        return diff > (pagingTouchSlop / viewState.currentScale)
    }

    private val calcDiffYToTop = fun(rect: Rectangle): Int {
        return (rect.bottom - viewState.viewport.bottom).roundToInt()
    }

    private val calcDiffYToBottom = fun(rect: Rectangle): Int {
        return (rect.top - viewState.viewport.top).roundToInt()
    }

    override fun populate() {
        Log.d(TAG, "populate!")

        val layoutManagerSnapshot = layoutManager ?: return

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewState)
        val scrollArea = currentRect?.calcScrollArea(
            tmpCurrentScrollArea,
            viewState.currentScale
        ) ?: return

        // detect overscroll
        if (scrollArea.contains(viewState.viewport)) {
            return
        }

        val toTop = (viewState.viewport.centerY < tmpCurrentScrollArea.centerY)

        val handled = if (toTop) {
            val topRect = layoutManagerSnapshot.topPageLayout(viewState)
            val topArea =
                topRect?.calcScrollArea(
                    tmpTopScrollArea,
                    viewState.currentScale
                )
            populateTo(
                scrollArea,
                topArea,
                shouldPopulateVertical,
                calcDiffHorizontal, calcDiffYToTop,
                populateDuration
            )
        } else {
            val bottomRect = layoutManagerSnapshot.bottomPageLayout(viewState)
            val bottomArea =
                bottomRect?.calcScrollArea(
                    tmpBottomScrollArea,
                    viewState.currentScale
                )
            populateTo(
                scrollArea,
                bottomArea,
                shouldPopulateVertical,
                calcDiffHorizontal, calcDiffYToBottom,
                populateDuration
            )
        }

        if (!handled) {
            populateToCurrent(
                scrollArea,
                reverseScrollDuration
            )
        }
    }

    override fun populateToTop(topRect: PageLayout) {
        val layoutManagerSnapshot = layoutManager ?: return

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewState)
        val scrollArea = currentRect?.calcScrollArea(
            tmpCurrentScrollArea,
            viewState.currentScale
        ) ?: return

        populateTo(
            scrollArea,
            topRect.calcScrollArea(tmpLeftScrollArea, viewState.currentScale),
            shouldPopulateVertical,
            calcDiffHorizontal, calcDiffYToTop,
            populateDuration
        )
    }

    override fun populateToBottom(bottomRect: PageLayout) {
        val layoutManagerSnapshot = layoutManager ?: return

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewState)
        val scrollArea = currentRect?.calcScrollArea(
            tmpCurrentScrollArea,
            viewState.currentScale
        ) ?: return

        populateTo(
            scrollArea,
            bottomRect.calcScrollArea(tmpLeftScrollArea, viewState.currentScale),
            shouldPopulateVertical,
            calcDiffHorizontal, calcDiffYToBottom,
            populateDuration
        )
    }

}
