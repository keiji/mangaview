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

interface OnReadFinishedListener {
    fun onReadFinished(mangaView: MangaView) {}
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

        private const val SCROLLING_DURATION = 280
        private const val REVERSE_SCROLLING_DURATION = 350
        private const val SCALING_DURATION = 250L
    }

    constructor(context: Context) : this(context, null, 0x0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0x0)

    private var scrollState: Int = -1
        set(value) {
            if (field != value) {
                Log.d(TAG, "scrollState $field -> $value")

                field = value
                onPageChangeListenerList.forEach {
                    it.onScrollStateChanged(this, value)
                }
            }

            if (value == SCROLL_STATE_IDLE) {
                currentPageLayout = layoutManager?.currentPageLayout(viewContext)
            }
        }

    private val viewConfiguration: ViewConfiguration = ViewConfiguration.get(context)
    private val density = context.resources.displayMetrics.scaledDensity

    private val pagingTouchSlop = viewConfiguration.scaledPagingTouchSlop * density

    private val onTapListenerList = ArrayList<OnTapListener>()

    fun addOnTapListener(onTapListener: OnTapListener) {
        onTapListenerList.add(onTapListener)
    }

    fun removeOnTapListener(onTapListener: OnTapListener) {
        onTapListenerList.remove(onTapListener)
    }

    private val onPageChangeListenerList = ArrayList<OnPageChangeListener>()

    fun addOnPageChangeListener(onPageChangeListener: OnPageChangeListener) {
        onPageChangeListenerList.add(onPageChangeListener)
    }

    fun removeOnPageChangeListener(onPageChangeListener: OnPageChangeListener) {
        onPageChangeListenerList.add(onPageChangeListener)
    }

    private val onReadFinishedListenerList = ArrayList<OnReadFinishedListener>()

    fun addOnReadFinishedListenerList(onReadFinishedListener: OnReadFinishedListener) {
        onReadFinishedListenerList.add(onReadFinishedListener)
    }

    fun removeOnReadFinishedListenerList(onReadFinishedListener: OnReadFinishedListener) {
        onReadFinishedListenerList.add(onReadFinishedListener)
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


    internal val viewContext = ViewContext()

    private var isInitialized = false

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

    private val currentScrollableArea: Rectangle?
        get() {
            return currentPageLayout?.calcScrollArea(viewContext, tmpCurrentScrollArea)
        }

    private val onDoubleTapListenerList = ArrayList<OnDoubleTapListener>()

    fun addOnDoubleTapListener(onDoubleTapListener: OnDoubleTapListener) {
        onDoubleTapListenerList.add(onDoubleTapListener)
    }

    fun removeOnDoubleTapListener(onDoubleTapListener: OnDoubleTapListener) {
        onDoubleTapListenerList.remove(onDoubleTapListener)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewContext.setViewSize(w, h)

        isInitialized = false
    }

    private fun init() {
        val layoutManagerSnapshot = layoutManager ?: return
        val adapterSnapshot = adapter ?: return

        layoutManagerSnapshot.adapter = adapterSnapshot
        layoutManagerSnapshot.pageLayoutManager = pageLayoutManager
        layoutManagerSnapshot.initWith(viewContext)

        pageLayoutManager.pageAdapter = adapterSnapshot

        scrollState = SCROLL_STATE_IDLE
        isInitialized = true
    }

    var onViewportChangeListener = object : OnContentViewportChangeListener {
        override fun onViewportChanged(
            mangaView: MangaView,
            layer: ContentLayer,
            viewport: RectF
        ): Boolean {
            return false
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!isInitialized) {
            init()
            showPage(currentPageIndex)
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

        val layoutManagerSnapshot = layoutManager ?: return
        val currentScrollAreaSnapshot = currentScrollableArea ?: return

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

            } else {
                viewContext.projectToScreenPosition(
                    viewContext.viewport.centerX,
                    viewContext.viewport.centerY,
                    tmpEventPoint
                )
                focusX = tmpEventPoint.centerX
                focusY = tmpEventPoint.centerY
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
            Log.d(TAG, "test")
            scrollState = SCROLL_STATE_IDLE
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

        // mapping global point
        val globalPosition = viewContext.projectToGlobalPosition(x, y, tmpEventPoint)

        onTapListenerList.forEach {
            handleOnTapListener(it, e.x, e.y, globalPosition)
        }

        return true
    }

    private fun handleOnTapListener(
        onTapListener: OnTapListener,
        x: Float,
        y: Float,
        globalPosition: Rectangle
    ) {
        var handled = onTapListener.onTap(this, x, y)
        if (handled) {
            return
        }

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
    }

    override fun onDown(e: MotionEvent?): Boolean = true

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

    internal var currentPageLayout: PageLayout? = null
        private set(value) {
            if (value == null || field == value) {
                return
            }

            field = value

            if (!value.containsPage(currentPageIndex)) {
                currentPageIndex = value.keyPage?.index ?: 0
                Log.d(TAG, "Update currentPageIndex $currentPageIndex")
            }

            onPageChangeListenerList.forEach {
                it.onPageLayoutSelected(this, value)
            }
        }

    private fun fling(velocityX: Float, velocityY: Float): Boolean {
        val layoutManagerSnapshot = layoutManager ?: return false

        val scaledVelocityX = velocityX / viewContext.currentScale
        val scaledVelocityY = velocityY / viewContext.currentScale

        val currentScrollAreaSnapshot = currentScrollableArea ?: return false

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
            val leftRect = layoutManagerSnapshot.leftPageLayout(viewContext, currentPageLayout)
            val rightRect = layoutManagerSnapshot.rightPageLayout(viewContext, currentPageLayout)

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
            val topRect = layoutManagerSnapshot.topPageLayout(viewContext, currentPageLayout)
            val bottomRect = layoutManagerSnapshot.bottomPageLayout(viewContext, currentPageLayout)

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
            currentScrollableArea
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
        populate()
    }

    private val populateOnScaleFinished = fun() {
        populate()
    }

    internal fun scale(
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

        // mapping global point
        val globalPosition = viewContext.projectToGlobalPosition(e.x, e.y, tmpEventPoint)

        onDoubleTapListenerList.forEach {
            handleOnDoubleTapListener(it, e.x, e.y, globalPosition)
        }

        return true
    }

    private fun handleOnDoubleTapListener(
        onDoubleTapListener: OnDoubleTapListener,
        x: Float,
        y: Float,
        globalPosition: Rectangle
    ) {
        var handled = onDoubleTapListener.onDoubleTap(this, x, y)
        if (handled) {
            return
        }

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
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        e ?: return false
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        e ?: return false

        return true
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
