package dev.keiji.mangaview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ScaleGestureDetectorCompat
import androidx.core.view.ViewCompat
import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle
import kotlin.math.abs
import kotlin.math.roundToInt

interface OnTapListener {
    fun onTap(mangaView: MangaView, x: Float, y: Float): Boolean = false
    fun onTap(page: Page, x: Float, y: Float): Boolean = false
    fun onTap(layer: ContentLayer, x: Float, y: Float): Boolean = false
}

interface OnDoubleTapListener {
    fun onDoubleTap(mangaView: MangaView, x: Float, y: Float): Boolean = false
    fun onDoubleTap(page: Page, x: Float, y: Float): Boolean = false
    fun onDoubleTap(layer: ContentLayer, x: Float, y: Float): Boolean = false
}

interface OnPageChangeListener {
    fun onScrollStateChanged(mangaView: MangaView, scrollState: Int) {}
    fun onPageLayoutSelected(mangaView: MangaView, pageLayout: PageLayout) {}
}

interface OnContentViewportChangeListener {
    fun onViewportChanged(mangaView: MangaView, layer: ContentLayer, viewport: RectF) = false
}

class MangaView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr),
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener,
    ScaleGestureDetector.OnScaleGestureListener {

    companion object {
        private val TAG = MangaView::class.java.simpleName

        const val SCROLL_STATE_IDLE = 0
        const val SCROLL_STATE_DRAGGING = 1
        const val SCROLL_STATE_SETTLING = 2

        private const val DOUBLE_TAP_ZOOM_SCALE = 4.0F

        private const val SCROLLING_DURATION = 280
        private const val REVERSE_SCROLLING_DURATION = 350
        private const val SCALING_DURATION = 250L

        private const val DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_HORIZONTAL = 0.2F
        private const val DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_VERTICAL = 0.2F
    }

    constructor(context: Context) : this(context, null, 0x0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0x0)

    private var scrollState: Int = SCROLL_STATE_IDLE
        set(value) {
            if (field == value) {
                return
            }
            field = value
            onPageChangeListener.onScrollStateChanged(this, value)
        }

    private val viewConfiguration: ViewConfiguration = ViewConfiguration.get(context)
    private val density = context.resources.displayMetrics.scaledDensity

    private val pagingTouchSlop = viewConfiguration.scaledPagingTouchSlop * density

    var onPageChangeListener = object : OnPageChangeListener {
        override fun onScrollStateChanged(mangaView: MangaView, scrollState: Int) {
            Log.d(TAG, "scrollState -> ${scrollState}")
        }

        override fun onPageLayoutSelected(mangaView: MangaView, pageLayout: PageLayout) {
            Log.d(TAG, "onPageLayoutSelected -> ${pageLayout.pages[0].index}")
        }
    }
    var layoutManager: LayoutManager? = null
        set(value) {
            field = value
            isInitialized = false
            postInvalidate()
        }

    var adapter: PageAdapter? = null
        set(value) {
            field = value
            isInitialized = false
            postInvalidate()
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var pageLayoutManager: PageLayoutManager = DoublePageLayoutManager(isSpread = true)
        set(value) {
            field = value
            isInitialized = false
            postInvalidate()
        }


    private val viewContext = ViewContext()

    private var isInitialized = false

    var doubleTapZoomEnabled = true

    @Suppress("MemberVisibilityCanBePrivate")
    var tapEdgeScrollEnabled = true

    private val tapEdgeScrollThresholdLeft = DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_HORIZONTAL
    private val tapEdgeScrollThresholdRight = 1.0F - DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_HORIZONTAL
    private val tapEdgeScrollThresholdTop = DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_VERTICAL
    private val tapEdgeScrollThresholdBottom = 1.0F - DEFAULT_TAP_EDGE_SCROLL_THRESHOLD_VERTICAL

    private val gestureDetector = GestureDetectorCompat(context, this).also {
        it.setOnDoubleTapListener(this)
    }
    private val scaleGestureDetector = ScaleGestureDetector(context, this).also {
        ScaleGestureDetectorCompat.setQuickScaleEnabled(it, false)
    }

    private val visiblePageLayoutList = ArrayList<PageLayout>()
    private val recycleBin = ArrayList<Page>()

    @Suppress("MemberVisibilityCanBePrivate")
    var paint = Paint().also {
        it.isAntiAlias = true
        it.isDither = true
    }

    private var scroller = OverScroller(context, DecelerateInterpolator())

    private var settleScroller = OverScroller(context, DecelerateInterpolator())

    enum class ScalingState {
        Begin,
        Scaling,
        End,
        Finish
    }

    private var scalingState = ScalingState.Finish
        set(value) {
            if (field == value) {
                return
            }
            Log.d(TAG, "ScalingState changed: $field -> $value")
            field = value
        }

    private var scaleInterpolator = DecelerateInterpolator()

    private var scaleOperation: ScaleOperation? = null

    private val scaleOperationInProgress: Boolean
        get() = scaleOperation != null

    var currentPageIndex: Int = 0

    private val tmpCurrentScrollArea = Rectangle()
    private val tmpEventPoint = Rectangle()

    private val currentScrollArea: Rectangle?
        get() {
            return currentPageLayout?.calcScrollArea(viewContext, tmpCurrentScrollArea)
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var onTapListener = object : OnTapListener {
        override fun onTap(page: Page, x: Float, y: Float): Boolean {
            Log.d(TAG, "onTap page:${page.index}, x:$x, y:$y")

            return false
        }

        override fun onTap(layer: ContentLayer, x: Float, y: Float): Boolean {
            Log.d(TAG, "onTap ${layer.page?.index}, layer, x:$x, y:$y")

            return false
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var onDoubleTapListener = object : OnDoubleTapListener {
        override fun onDoubleTap(mangaView: MangaView, x: Float, y: Float): Boolean {
            return false
        }

        override fun onDoubleTap(page: Page, x: Float, y: Float): Boolean {
            return false
        }

        override fun onDoubleTap(layer: ContentLayer, x: Float, y: Float): Boolean {
            return false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewContext.setViewSize(w, h)
        layoutManager?.also {
            it.setViewSize(w, h)
            viewContext.offsetTo(it.initialScrollX, it.initialScrollY)
        }

        isInitialized = false
    }

    private fun init() {
        val layoutManagerSnapshot = layoutManager ?: return
        val adapterSnapshot = adapter ?: return

        layoutManagerSnapshot.adapter = adapterSnapshot
        layoutManagerSnapshot.pageLayoutManager = pageLayoutManager
        layoutManagerSnapshot.setScrollableAxis(viewContext)

        pageLayoutManager.pageAdapter = adapterSnapshot

        showPage(currentPageIndex)

        isInitialized = true
    }

    var onViewportChangeListener = object : OnContentViewportChangeListener {
        override fun onViewportChanged(
            mangaView: MangaView,
            layer: ContentLayer,
            viewport: RectF
        ): Boolean {
            Log.d(TAG, "onViewportChanged: ${layer.page?.index}", viewport)
            return false
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!isInitialized) {
            init()
            postInvalidate()
            return
        }

        recycleBin.addAll(visiblePageLayoutList.flatMap { it.pages })

        layoutManager?.obtainVisiblePageLayout(viewContext, visiblePageLayoutList)

        val result = visiblePageLayoutList
            .flatMap { it.pages }
            .map { page ->
                recycleBin.remove(page)
                if (!page.globalRect.intersect(viewContext.viewport)) {
                    return@map true
                }
                page.draw(
                    canvas,
                    viewContext,
                    paint
                ) { layer: ContentLayer, viewport: RectF ->
                    onViewportChangeListener.onViewportChanged(this, layer, viewport)
                }
            }.none { !it }

        recycleBin.forEach { page ->
            page.recycle()
        }
        recycleBin.clear()

        if (!result) {
            postInvalidate()
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
        // TODO
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        // TODO
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun showPage(pageIndex: Int, smoothScroll: Boolean = false) {
        val pageLayoutIndex = pageLayoutManager.calcPageLayoutIndex(pageIndex)
        val pageLayout = layoutManager?.getPageLayout(pageLayoutIndex, viewContext)

        if (pageLayout == null) {
            Log.d(TAG, "pageIndex: ${pageIndex} -> pageLayoutIndex ${pageLayoutIndex} not found.")
            return
        }

        val scrollArea = pageLayout.globalPosition

        if (!smoothScroll) {
            scale(
                1.0F,
                viewContext.viewport.centerY,
                viewContext.viewport.centerY,
                smoothScale = false
            )
            viewContext.offsetTo(scrollArea.left, scrollArea.top)
            layoutManager?.obtainVisiblePageLayout(viewContext, visiblePageLayoutList)

            postInvalidate()
            return
        }

        scale(
            viewContext.minScale,
            null, null,
            smoothScale = true
        ) {
            val currentLeft = viewContext.viewport.left.roundToInt()
            val currentTop = viewContext.viewport.top.roundToInt()

            settleScroller.startScroll(
                currentLeft,
                currentTop,
                scrollArea.left.roundToInt() - currentLeft,
                scrollArea.top.roundToInt() - currentTop,
                SCROLLING_DURATION
            )
            startAnimation()
        }

        startAnimation()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                abortAnimation()
            }
            MotionEvent.ACTION_UP -> {
                if (scrollState != SCROLL_STATE_SETTLING) {
                    scrollState = SCROLL_STATE_IDLE
                }
                populate()
            }
        }

        if (scaleGestureDetector.onTouchEvent(event)) {
            postInvalidate()
        }

        if (gestureDetector.onTouchEvent(event)) {
            postInvalidate()
            return true
        }

        return super.onTouchEvent(event)
    }

    private fun populate() {
        if (scaleOperationInProgress) {
            return
        }

        Log.d(TAG, "populate")
        val layoutManagerSnapshot = layoutManager ?: return
        val currentScrollAreaSnapshot = currentScrollArea ?: return

        layoutManagerSnapshot.populateHelper
            .init(
                viewContext,
                layoutManagerSnapshot,
                settleScroller,
                pagingTouchSlop,
                SCROLLING_DURATION,
                REVERSE_SCROLLING_DURATION
            )
            .populateToCurrent(currentScrollAreaSnapshot, SCROLLING_DURATION)
        startAnimation()

        scalingState = ScalingState.Finish
    }

    private fun startAnimation() {
        ViewCompat.postInvalidateOnAnimation(this)
    }

    private fun abortAnimation() {
        scroller.abortAnimation()
        settleScroller.abortAnimation()
        scaleOperation = null
    }

    override fun computeScroll() {
        super.computeScroll()

        if (!isInitialized) {
            return
        }

        val needPostInvalidateScale = scaleOperation?.let {
            val elapsed = System.currentTimeMillis() - it.startTimeMillis
            val input = elapsed.toFloat() / it.durationMillis
            val scaleFactor = scaleInterpolator.getInterpolation(input)
            val newScale = it.from + it.diff * scaleFactor

            val focusX: Float
            val focusY: Float

            if (it.focusX != null && it.focusY != null) {
                focusX = it.focusX
                focusY = it.focusY
                Log.d(TAG, "1 focusX: $focusX, focusY: $focusY")

            } else {
                viewContext.projectToScreenPosition(
                    viewContext.viewport.centerX,
                    viewContext.viewport.centerY,
                    tmpEventPoint
                )
                focusX = tmpEventPoint.centerX
                focusY = tmpEventPoint.centerY
                Log.d(TAG, "2 focusX: $focusX, focusY: $focusY")
            }

            viewContext.scaleTo(newScale, focusX, focusY)

            val needPostInvalidateScale = if (input > 1.0F || elapsed <= 0) {
                viewContext.scaleTo(it.to, focusX, focusY)
                scaleOperation = null
                it.onScaleFinished()
                false
            } else {
                true
            }

            return@let needPostInvalidateScale
        } ?: false

        val needPostInvalidateScroll =
            if (scroller.isFinished && !settleScroller.isFinished && settleScroller.computeScrollOffset()) {
                viewContext.offsetTo(settleScroller.currX.toFloat(), settleScroller.currY.toFloat())
                scrollState = SCROLL_STATE_SETTLING
                !settleScroller.isFinished

            } else if (!scroller.isFinished && scroller.computeScrollOffset()) {
                viewContext.offsetTo(scroller.currX.toFloat(), scroller.currY.toFloat())
                !scroller.isFinished
            } else {
                false
            }

        if (!needPostInvalidateScroll && scrollState == SCROLL_STATE_SETTLING) {
            scrollState = SCROLL_STATE_IDLE
        }

        layoutManager?.currentPageLayout(viewContext)?.also {
            currentPageLayout = it
        }

        if (needPostInvalidateScale || needPostInvalidateScroll) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onShowPress(e: MotionEvent?) {
        Log.d(TAG, "onShowPress")
        abortAnimation()
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        e ?: return false

        return true
    }

    override fun onDown(e: MotionEvent?): Boolean {
        e ?: return false

        tapToScroll(e)

        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val handled = fling(velocityX, velocityY)
        if (handled) {
            startAnimation()
        }
        return handled
    }

    private var currentPageLayout: PageLayout? = null
        private set(value) {
            if (value == null || field == value || scrollState != SCROLL_STATE_IDLE) {
                return
            }

            field = value

            if (!value.containsPage(currentPageIndex)) {
                currentPageIndex = value.keyPage?.index ?: 0
                Log.d(TAG, "Update currentPageIndex $currentPageIndex")
            }

            onPageChangeListener.onPageLayoutSelected(this, value)
        }

    private fun fling(velocityX: Float, velocityY: Float): Boolean {
        val layoutManagerSnapshot = layoutManager ?: return false

        val scaledVelocityX = velocityX / viewContext.currentScale
        val scaledVelocityY = velocityY / viewContext.currentScale

        Log.d(TAG, "scaledVelocityX $scaledVelocityX")
        Log.d(TAG, "scaledVelocityY $scaledVelocityY")

        val currentPageLayoutSnapshot = currentPageLayout ?: return false
        val currentScrollAreaSnapshot = currentScrollArea ?: return false

        Log.d(TAG, "fling ${currentPageLayoutSnapshot.pages[0].index}")

        val populateHelper = layoutManagerSnapshot.populateHelper
            .init(
                viewContext,
                layoutManagerSnapshot,
                settleScroller,
                pagingTouchSlop,
                SCROLLING_DURATION,
                REVERSE_SCROLLING_DURATION
            )

        var handleHorizontal = false
        var handleVertical = false

        val horizontal = (abs(scaledVelocityX) > abs(scaledVelocityY))

        if (horizontal) {
            val leftRect = layoutManagerSnapshot.leftPageLayout(viewContext)
            val rightRect = layoutManagerSnapshot.rightPageLayout(viewContext)

            handleHorizontal = if (scaledVelocityX > 0.0F && leftRect != null
                && !viewContext.canScrollLeft(currentScrollAreaSnapshot)
            ) {
                Log.d(TAG, "populateToLeft")
                populateHelper.populateToLeft(leftRect)
                true
            } else if (scaledVelocityX < 0.0F && rightRect != null
                && !viewContext.canScrollRight(currentScrollAreaSnapshot)
            ) {
                Log.d(TAG, "populateToRight")
                populateHelper.populateToRight(rightRect)
                true
            } else {
                false
            }
        } else {
            val topRect = layoutManagerSnapshot.topPageLayout(viewContext)
            val bottomRect = layoutManagerSnapshot.bottomPageLayout(viewContext)

            handleVertical = if (scaledVelocityY > 0.0F && topRect != null
                && !viewContext.canScrollTop(currentScrollAreaSnapshot)
            ) {
                populateHelper.populateToTop(topRect)
                true
            } else if (scaledVelocityY < 0.0F && bottomRect != null
                && !viewContext.canScrollBottom(currentScrollAreaSnapshot)
            ) {
                populateHelper.populateToBottom(bottomRect)
                true
            } else {
                false
            }
        }

        if (handleHorizontal || handleVertical) {
            return true
        }

        val viewport = viewContext.viewport

        val minX = currentScrollAreaSnapshot.left.roundToInt()
        val maxX = (currentScrollAreaSnapshot.right - viewport.width).roundToInt()

        val minY = currentScrollAreaSnapshot.top.roundToInt()
        val maxY = (currentScrollAreaSnapshot.bottom - viewport.height).roundToInt()

        Log.d(
            TAG, "fling " +
                    "currentX ${viewContext.currentX}, currentY ${viewContext.currentY}, " +
                    "scaledVelocityX ${scaledVelocityX}, scaledVelocityY ${scaledVelocityY}, " +
                    "minX ${minX}, maxX ${maxX}, " +
                    "minY ${minY}, maxY ${maxY}"
        )

        // Do not fling if over-scrolled
        if (horizontal
            && (viewport.left < currentScrollAreaSnapshot.left
                    || viewport.right > currentScrollAreaSnapshot.right)
        ) {
            return false
        } else if (viewport.top < currentScrollAreaSnapshot.top
            || viewport.bottom > currentScrollAreaSnapshot.bottom
        ) {
            return false
        }

        scroller.fling(
            viewContext.currentX.roundToInt(),
            viewContext.currentY.roundToInt(),
            -scaledVelocityX.roundToInt(),
            -scaledVelocityY.roundToInt(),
            minX, maxX,
            minY, maxY,
        )

        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        viewContext.scroll(
            distanceX / viewContext.currentScale,
            distanceY / viewContext.currentScale,
            currentScrollArea
        )

        scrollState = SCROLL_STATE_DRAGGING

        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.d(TAG, "onLongPress")
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        detector ?: return false

        scalingState = ScalingState.Begin

        return true
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        detector ?: return false

        Log.d(TAG, "onScale focusX:${detector.focusX} ,focusY:${detector.focusY}")
        scalingState = ScalingState.Scaling
        viewContext.scale(detector.scaleFactor, detector.focusX, detector.focusY)

        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        detector ?: return

        scalingState = ScalingState.End
    }

    private val populateOnScaleFinished = fun() {
        populate()
    }

    private fun scale(
        scale: Float,
        focusX: Float?,
        focusY: Float?,
        smoothScale: Boolean = false,
        onScaleFinished: () -> Unit = populateOnScaleFinished,
    ) {
        if (!smoothScale) {
            viewContext.projectToScreenPosition(
                viewContext.viewport.centerX,
                viewContext.viewport.centerY,
                tmpEventPoint
            )
            viewContext.scaleTo(
                scale,
                focusX ?: tmpEventPoint.centerX,
                focusY ?: tmpEventPoint.centerY
            )
            postInvalidate()
            return
        }

        scaleOperation = ScaleOperation(
            viewContext.currentScale, scale,
            System.currentTimeMillis(), SCALING_DURATION,
            focusX, focusY, onScaleFinished
        )

        startAnimation()
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        e ?: return false

        doubleTapZoom(e)

        var handled = onDoubleTapListener.onDoubleTap(this, e.x, e.y)
        if (handled) {
            return true
        }

        // mapping global point
        val globalPosition = viewContext.projectToGlobalPosition(e.x, e.y, tmpEventPoint)

        visiblePageLayoutList
            .flatMap { it.pages }
            .forEach pageLoop@{ page ->
                handled = page.requestHandleEvent(
                    globalPosition.centerX,
                    globalPosition.centerY,
                    onDoubleTapListener
                )
                if (handled) {
                    return@pageLoop
                }

                page.layers.forEach { layer ->
                    handled = layer.requestHandleEvent(
                        globalPosition.centerX,
                        globalPosition.centerY,
                        onDoubleTapListener
                    )
                    if (handled) {
                        return@pageLoop
                    }
                }
            }

        return true
    }

    private fun doubleTapZoom(e: MotionEvent): Boolean {
        if (!doubleTapZoomEnabled) {
            return false
        }

        val scale = DOUBLE_TAP_ZOOM_SCALE

        if (viewContext.currentScale >= scale) {
            scale(viewContext.minScale, e.x, e.y, smoothScale = true)
        } else {
            scale(scale, e.x, e.y, smoothScale = true)
        }

        return true
    }

    private fun tapToScroll(e: MotionEvent): Boolean {
        if (!tapEdgeScrollEnabled) {
            return false
        }

        val currentScrollAreaSnapshot = currentPageLayout?.scrollArea ?: return false
        if (!viewContext.viewport.contains(currentScrollAreaSnapshot)) {
            return false
        }

        val layoutManagerSnapshot = layoutManager ?: return false

        if (e.x < viewContext.viewWidth * tapEdgeScrollThresholdLeft) {
            toLeftPage(layoutManagerSnapshot)
            return true
        }

        if (e.x > viewContext.viewWidth * tapEdgeScrollThresholdRight) {
            toRightPage(layoutManagerSnapshot)
            return true
        }

        if (e.y < viewContext.viewHeight * tapEdgeScrollThresholdTop) {
            toTopPage(layoutManagerSnapshot)
            return true
        }

        if (e.x > viewContext.viewHeight * tapEdgeScrollThresholdBottom) {
            toBottomPage(layoutManagerSnapshot)
            return true
        }

        return false
    }

    private fun toLeftPage(
        layoutManager: LayoutManager
    ): Boolean {
        val index = layoutManager.leftPageLayout(viewContext)
            ?.keyPage?.index ?: return false
        showPage(index, smoothScroll = true)
        return true
    }

    private fun toRightPage(
        layoutManager: LayoutManager
    ): Boolean {
        val index = layoutManager.rightPageLayout(viewContext)
            ?.keyPage?.index ?: return false
        showPage(index, smoothScroll = true)
        return true
    }

    private fun toTopPage(
        layoutManager: LayoutManager
    ): Boolean {
        val index = layoutManager.topPageLayout(viewContext)
            ?.keyPage?.index ?: return false
        showPage(index, smoothScroll = true)
        return false
    }

    private fun toBottomPage(
        layoutManager: LayoutManager
    ): Boolean {
        val index = layoutManager.topPageLayout(viewContext)
            ?.keyPage?.index ?: return false
        showPage(index, smoothScroll = true)
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        e ?: return false
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        e ?: return false
        Log.d(TAG, "onSingleTapConfirmed")

        var handled = onTapListener.onTap(this, e.x, e.y)
        if (handled) {
            return true
        }

        // mapping global point
        val globalPosition = viewContext.projectToGlobalPosition(e.x, e.y, tmpEventPoint)

        visiblePageLayoutList
            .flatMap { it.pages }
            .forEach pageLoop@{ page ->
                handled = page.requestHandleEvent(
                    globalPosition.centerX,
                    globalPosition.centerY,
                    onTapListener
                )
                if (handled) {
                    return@pageLoop
                }

                page.layers.forEach { layer ->
                    handled = layer.requestHandleEvent(
                        globalPosition.centerX,
                        globalPosition.centerY,
                        onTapListener
                    )
                    if (handled) {
                        return@pageLoop
                    }
                }
            }
        return false
    }

    private data class ScaleOperation(
        val from: Float,
        val to: Float,
        val startTimeMillis: Long,
        val durationMillis: Long,
        val focusX: Float?,
        val focusY: Float?,
        val onScaleFinished: () -> Unit
    ) {
        val diff: Float = to - from
    }
}
