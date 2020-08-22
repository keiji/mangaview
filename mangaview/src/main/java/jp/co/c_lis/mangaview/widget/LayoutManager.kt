package jp.co.c_lis.mangaview.widget

import androidx.collection.SparseArrayCompat
import kotlin.math.max
import kotlin.math.min

abstract class LayoutManager {

    companion object {
        private val TAG = LayoutManager::class.java.simpleName
    }

    internal abstract val populateHelper: PopulateHelper

    internal lateinit var adapter: PageAdapter
    internal lateinit var pageLayoutManager: PageLayoutManager

    var viewWidth: Int = 0
    var viewHeight: Int = 0

    fun setViewSize(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height

        init()
    }

    abstract fun init()

    abstract fun currentPageLayoutIndex(viewState: ViewState): Int

    fun getPageLayout(index: Int, viewState: ViewState): PageLayout? {
        if (index < 0) {
            return null
        }
        if (index >= pageLayoutManager.getCount()) {
            return null
        }

        val pageLayout = caches[index] ?: layout(
            index,
            pageLayoutManager.createPageLayout(),
            viewState
        )
        caches.put(index, pageLayout)
        return pageLayout
    }

    fun currentPageLayout(viewState: ViewState): PageLayout? {
        return getPageLayout(currentPageLayoutIndex(viewState), viewState)
    }

    open fun leftPageLayout(viewState: ViewState): PageLayout? = null
    open fun rightPageLayout(viewState: ViewState): PageLayout? = null
    open fun topPageLayout(viewState: ViewState): PageLayout? = null
    open fun bottomPageLayout(viewState: ViewState): PageLayout? = null

    private val caches = SparseArrayCompat<PageLayout>()

    fun visiblePages(
        viewState: ViewState,
        resultList: ArrayList<PageLayout> = ArrayList(),
        offsetScreenPageLimit: Int = 1
    ): List<PageLayout> {
        val firstVisiblePageLayoutIndex = calcFirstVisiblePageLayoutIndex(viewState)
        val endVisiblePageLayoutIndex = calcLastVisiblePageLayoutIndex(viewState)

        var startIndex = min(endVisiblePageLayoutIndex, firstVisiblePageLayoutIndex)
        var endIndex = max(endVisiblePageLayoutIndex, firstVisiblePageLayoutIndex)

        startIndex -= offsetScreenPageLimit
        endIndex += offsetScreenPageLimit

        startIndex = max(0, startIndex)
        endIndex = min(
            endIndex,
            pageLayoutManager.getCount() - 1
        )

        resultList.clear()

        (startIndex..endIndex).forEach { index ->
            val pageLayout = getPageLayout(index, viewState) ?: return@forEach
            if (!pageLayout.isFilled) {
                pageLayoutManager.layout(pageLayout, index)
            }
            resultList.add(pageLayout)
        }

        return resultList
    }

    abstract val initialScrollX: Float
    abstract val initialScrollY: Float

    abstract fun layout(index: Int, pageLayout: PageLayout, viewState: ViewState): PageLayout

    abstract fun calcFirstVisiblePageLayoutIndex(viewState: ViewState): Int

    abstract fun calcLastVisiblePageLayoutIndex(viewState: ViewState): Int
}
