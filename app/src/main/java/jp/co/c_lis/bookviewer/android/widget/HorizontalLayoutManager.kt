package jp.co.c_lis.bookviewer.android.widget

class HorizontalLayoutManager(
    private val reversed: Boolean = false
) : LayoutManager() {

    companion object {
        private val TAG = HorizontalLayoutManager::class.java.simpleName
    }

    override fun layout(viewState: ViewState) {

        // layout pages
        for (index in pageList.indices) {
            val page = pageList[index]

            val positionLeft = if (!reversed) {
                index
            } else {
                -index
            }

            page.position.also {
                it.left = positionLeft.toFloat()
                it.right = it.left + 1.0F
                it.top = 0.0F
                it.bottom = 1.0F
            }
        }

        viewState.scrollableArea.also { area ->
            area.left = pageList.minBy { it.position.left }?.position?.left ?: 0.0F
            area.right = pageList.maxBy { it.position.right }?.position?.right ?: 0.0F
            area.top = pageList.minBy { it.position.top }?.position?.top ?: 0.0F
            area.bottom = pageList.maxBy { it.position.bottom }?.position?.bottom ?: 0.0F
        }
    }
}

