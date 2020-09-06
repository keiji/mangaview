package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Rectangle
import kotlin.math.sign

abstract class PopulateHelper {

    companion object {
        private val TAG = PopulateHelper::class.java.simpleName
    }

    internal lateinit var viewContext: ViewContext
    internal var layoutManager: LayoutManager? = null

    internal var pagingTouchSlop: Float = 0.0F

    private var duration: Long = 0

    private val tmp = Rectangle()

    fun init(
        viewContext: ViewContext,
        layoutManager: LayoutManager,
        pagingTouchSlop: Float,
        duration: Long,
    ): PopulateHelper {
        this.viewContext = viewContext
        this.layoutManager = layoutManager
        this.pagingTouchSlop = pagingTouchSlop
        this.duration = duration
        return this
    }

    fun populateTo(
        from: PageLayout?,
        to: PageLayout?,
        shouldPopulate: (Rectangle?) -> Boolean,
        calcDestRectangle: (ViewContext, ViewContext, Rectangle, Rectangle) -> Rectangle,
        scale: Float = viewContext.currentScale
    ): Animator? {
        from ?: return null
        to ?: return null

        val overlap = Rectangle.and(
            from.getScaledScrollArea(viewContext),
            viewContext.viewport,
            tmp
        )

        if (!shouldPopulate(overlap)) {
            return null
        }

        val scaledViewContext = if (viewContext.currentScale == scale) {
            viewContext
        } else {
            viewContext.copy().also {
                it.scaleTo(scale)
            }
        }

        val scrollableArea = to.getScaledScrollArea(scaledViewContext)

        val destRectangle = calcDestRectangle(viewContext, scaledViewContext, scrollableArea, tmp)

        return animator.populateTo(
            viewContext,
            to,
            scrollableArea,
            destRectangle,
            durationMillis = duration
        )
    }

    private val animator = Animator()

    fun populateToCurrent(pageLayout: PageLayout?): Animator? {
        pageLayout ?: return null

        return animator.populateTo(
            viewContext,
            pageLayout,
            pageLayout.getScaledScrollArea(viewContext),
            durationMillis = duration
        )
    }

    open fun populateToLeft(
        leftRect: PageLayout,
        scale: Float = viewContext.currentScale
    ): Animator? {
        return null
    }

    open fun populateToRight(
        rightRect: PageLayout,
        scale: Float = viewContext.currentScale
    ): Animator? {
        return null
    }

    open fun populateToTop(
        topRect: PageLayout,
        scale: Float = viewContext.currentScale
    ): Animator? {
        return null
    }

    open fun populateToBottom(
        bottomRect: PageLayout,
        scale: Float = viewContext.currentScale
    ): Animator? {
        return null
    }
}
