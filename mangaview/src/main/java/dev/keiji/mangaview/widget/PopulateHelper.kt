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

    internal var resetScaleOnPageChanged = true

    private var duration: Long = 0

    val tmp = Rectangle()

    fun init(
        viewContext: ViewContext,
        layoutManager: LayoutManager,
        pagingTouchSlop: Float,
        duration: Long,
        resetScaleOnPageChanged: Boolean = false
    ): PopulateHelper {
        this.viewContext = viewContext
        this.layoutManager = layoutManager
        this.pagingTouchSlop = pagingTouchSlop
        this.duration = duration
        this.resetScaleOnPageChanged = resetScaleOnPageChanged
        return this
    }

    fun populateTo(
        from: PageLayout?,
        to: PageLayout?,
        shouldPopulate: (Rectangle?) -> Boolean,
        calcDestRectangle: (ViewContext, PageLayout, Float, Rectangle) -> Rectangle,
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

        val destRectangle = calcDestRectangle(viewContext, to, scale, tmp)

        return animator.populateTo(
            viewContext,
            to,
            scale,
            destRectangle,
            durationMillis = duration
        )
    }

    private val animator = Animator()

    fun populateToCurrent(pageLayout: PageLayout?): Animator? {
        return animator.populateTo(viewContext, pageLayout)
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
