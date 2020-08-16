package jp.co.c_lis.bookviewer.android.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ScaleGestureDetectorCompat
import androidx.core.view.ViewCompat
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class BookView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr),
    GestureDetector.OnGestureListener,
    ScaleGestureDetector.OnScaleGestureListener,
    GestureDetector.OnDoubleTapListener {

    companion object {
        private val TAG = BookView::class.java.simpleName

        private const val SCROLLING_DURATION = 250
        private const val SCALING_DURATION = 350L
    }

    constructor(context: Context) : this(context, null, 0x0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0x0)

    private val viewConfiguration: ViewConfiguration = ViewConfiguration.get(context)
    private val density = context.resources.displayMetrics.scaledDensity

    private val pagingTouchSlop = viewConfiguration.scaledPagingTouchSlop * density

    var layoutManager: LayoutManager? = null
        set(value) {
            field = value
            invalidate()
        }

    var adapter: PageAdapter? = null
        set(value) {
            field = value
            isInitialized = false
            invalidate()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewState.also {
            it.viewWidth = w.toFloat()
            it.viewHeight = h.toFloat()

            it.viewport.left = 0.0F
            it.viewport.top = 0.0F
            it.viewport.right = it.viewWidth
            it.viewport.bottom = it.viewHeight
        }
        isInitialized = false
    }

    private fun init() {
        val adapterSnapshot = adapter ?: return
        val layoutManagerSnapshot = layoutManager ?: return

        layoutManagerSnapshot.pageList = (0 until adapterSnapshot.getPageCount())
            .map { adapterSnapshot.getPage(it) }

        layoutManagerSnapshot.layout(viewState)
        viewState.viewport
        isInitialized = true
    }

    private val viewState = ViewState()

    private var isInitialized = false

    @Suppress("MemberVisibilityCanBePrivate")
    var paint = Paint().also {
        it.isAntiAlias = true
        it.isDither = true
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var coroutineScope = CoroutineScope(Dispatchers.Main)

    private val visiblePages = ArrayList<Page>()
    private val recycleBin = ArrayList<Page>()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!isInitialized) {
            init()
            invalidate()
            return
        }

        var result = true

        recycleBin.addAll(visiblePages)
        layoutManager?.visiblePages(viewState, visiblePages)

        visiblePages.forEach { page ->
            if (!page.position.intersect(viewState.viewport)) {
                return@forEach
            }

            if (!page.draw(canvas, viewState, paint, coroutineScope)) {
                result = false
            }
        }

        coroutineScope.launch(Dispatchers.Unconfined) {
            synchronized(recycleBin) {
                recycleBin.forEach {
                    if (!visiblePages.contains(it)) {
                        it.recycle()
                    }
                }
                recycleBin.clear()
            }
        }

        if (!result) {
            invalidate()
        }
    }

    fun showPage(pageIndex: Int, smoothScroll: Boolean = false) {
        val layoutManagerSnapshot = layoutManager ?: return

        val rect = layoutManagerSnapshot.getPageRect(pageIndex)

        if (!smoothScroll) {
            viewState.offsetTo(
                rect.left.roundToInt(),
                rect.top.roundToInt()
            )
            invalidate()
            return
        }

        val currentLeft = viewState.viewport.left.roundToInt()
        val currentTop = viewState.viewport.top.roundToInt()

        scroller.startScroll(
            currentLeft, currentTop,
            rect.left.roundToInt() - currentLeft, rect.top.roundToInt() - currentTop,
            SCROLLING_DURATION
        )

        startAnimation()
    }

    private val gestureDetector = GestureDetectorCompat(context, this).also {
        it.setOnDoubleTapListener(this)
    }
    private val scaleGestureDetector = ScaleGestureDetector(context, this).also {
        ScaleGestureDetectorCompat.setQuickScaleEnabled(it, false)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (scaleGestureDetector.onTouchEvent(event)) {
            invalidate()
        }
        if (gestureDetector.onTouchEvent(event)) {
            invalidate()
            return true
        }

        event ?: return super.onTouchEvent(event)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                abortAnimation()
            }
            MotionEvent.ACTION_UP -> {
                delayedPopulate()
            }
        }
        return super.onTouchEvent(event)
    }

    private var delayedPopulate: Job? = null

    private fun delayedPopulate() {
        delayedPopulate = coroutineScope.launch(Dispatchers.Unconfined) {
            delay(200)
            withContext(Dispatchers.Main) {
                populate()
            }
        }
    }

    private val populateTmp = Rectangle()

    private val shouldPopulateHorizontal = fun(rect: Rectangle) = rect.width > pagingTouchSlop

    private val calcDiffBlank = fun(_: Rectangle) = 0

    private val calcDiffXToLeft = fun(rect: Rectangle): Int {
        return -(viewState.viewport.width - rect.width).roundToInt()
    }
    private val calcDiffXToRight = fun(rect: Rectangle): Int {
        return (viewState.viewport.width - rect.width).roundToInt()
    }

    private val shouldPopulateVertical = fun(rect: Rectangle) = rect.height > pagingTouchSlop

    private val calcDiffXToTop = fun(rect: Rectangle): Int {
        return -(viewState.viewport.height - rect.height).roundToInt()
    }
    private val calcDiffXToBottom = fun(rect: Rectangle): Int {
        return (viewState.viewport.height - rect.height).roundToInt()
    }

    private fun populate() {
        Log.d(TAG, "populate!")

        if (scaling != null) {
            return
        }

        val layoutManagerSnapshot = layoutManager ?: return

        val leftRect = layoutManagerSnapshot.leftRect(viewState)
        val rightRect = layoutManagerSnapshot.rightRect(viewState)
        val topRect = layoutManagerSnapshot.topRect(viewState)
        val bottomRect = layoutManagerSnapshot.bottomRect(viewState)

        val result = tryPopulate(
            leftRect,
            shouldPopulateHorizontal,
            calcDiffXToLeft, calcDiffBlank
        ) or tryPopulate(
            rightRect,
            shouldPopulateHorizontal,
            calcDiffXToRight, calcDiffBlank
        ) or tryPopulate(
            topRect,
            shouldPopulateVertical,
            calcDiffBlank, calcDiffXToTop
        ) or tryPopulate(
            bottomRect,
            shouldPopulateVertical,
            calcDiffBlank, calcDiffXToBottom
        )

        if (!result) {
            Log.d(TAG, "populate to current")

            val toLeft = (leftRect != null && rightRect == null)
            val toTop = (topRect != null && bottomRect == null)

            val calcDiffXToCurrent = if (toLeft) {
                calcDiffXToRight
            } else {
                calcDiffXToLeft
            }
            val calcDiffYToCurrent = if (toTop) {
                calcDiffXToBottom
            } else {
                calcDiffXToTop
            }

            tryPopulate(
                layoutManagerSnapshot.currentRect(viewState),
                { _ -> true },
                calcDiffXToCurrent, calcDiffYToCurrent
            )
        }

        startAnimation()
    }

    private fun tryPopulate(
        rect: Rectangle?,
        shouldPopulate: (Rectangle) -> Boolean,
        dx: (Rectangle) -> Int,
        dy: (Rectangle) -> Int
    ): Boolean {
        rect ?: return false

        populateTmp.set(0.0F, 0.0F, 0.0F, 0.0F)
        Rectangle.and(rect, viewState.viewport, populateTmp)

        if (shouldPopulate(populateTmp)) {
            val startX = viewState.viewport.left.roundToInt()
            val startY = viewState.viewport.top.roundToInt()

            settleScroller.startScroll(
                startX,
                startY,
                dx(populateTmp),
                dy(populateTmp),
                SCROLLING_DURATION
            )
            return true
        }

        return false
    }

    private var settleScroller = OverScroller(context, DecelerateInterpolator())
    private var scroller = OverScroller(context, DecelerateInterpolator())

    private fun startAnimation() {
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private fun abortAnimation() {
        scroller.abortAnimation()
    }

    private var scaleInterpolator = DecelerateInterpolator()

    private var scaling: Scaling? = null

    override fun computeScroll() {
        super.computeScroll()

        if (!scroller.isFinished && scroller.computeScrollOffset()) {
            viewState.offsetTo(scroller.currX, scroller.currY)
            ViewCompat.postInvalidateOnAnimation(this)
        }

        if (!settleScroller.isFinished && settleScroller.computeScrollOffset()) {
            viewState.offsetTo(settleScroller.currX, settleScroller.currY)
            ViewCompat.postInvalidateOnAnimation(this)
        }

        scaling?.also {
            val elapsed = System.currentTimeMillis() - it.startTimeMillis
            val input = elapsed.toFloat() / it.durationMillis
            val scaleFactor = scaleInterpolator.getInterpolation(input)
            val newScale = it.from + it.diff * scaleFactor
            viewState.setScale(newScale, it.focusX, it.focusY)

            if (input > 1.0F) {
                scaling = null
            }

            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onShowPress(e: MotionEvent?) = abortAnimation()

    override fun onSingleTapUp(e: MotionEvent?) = false

    override fun onDown(e: MotionEvent?) = true

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean = fling(velocityX, velocityY)

    private fun fling(velocityX: Float, velocityY: Float): Boolean {
        val layoutManagerSnapshot = layoutManager ?: return false

        val scaledVelocityX = velocityX / viewState.currentScale
        val scaledVelocityY = velocityY / viewState.currentScale

        val currentRect = layoutManagerSnapshot.currentRect(viewState)

        val minX = currentRect.left.roundToInt()
        val maxX = (currentRect.right - viewState.width).roundToInt()

        val minY = currentRect.top.roundToInt()
        val maxY = (currentRect.bottom - viewState.height).roundToInt()

        scroller.fling(
            viewState.scrollX.roundToInt(),
            viewState.scrollY.roundToInt(),
            -scaledVelocityX.roundToInt(),
            -scaledVelocityY.roundToInt(),
            minX, maxX,
            minY, maxY
        )

        startAnimation()

        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.d(TAG, "onScroll")
        return viewState.onScroll(distanceX, distanceY)
    }

    override fun onLongPress(e: MotionEvent?) {
        Log.d(TAG, "onLongPress")
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        detector ?: return false

        Log.d(TAG, "onScaleBegin")
        return viewState.onScaleBegin()
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        detector ?: return false

        Log.d(TAG, "onScale")
        return viewState.onScale(detector.scaleFactor, detector.focusX, detector.focusY)
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        detector ?: return

        Log.d(TAG, "onScaleEnd")
        viewState.onScaleEnd()

        delayedPopulate()
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        e ?: return false

        delayedPopulate?.cancel()
        delayedPopulate = null

        Log.d(TAG, "onDoubleTap")

        scale(2.5F, e.x, e.y, smoothScale = true)
        return true
    }

    private fun scale(scale: Float, focusX: Float, focusY: Float, smoothScale: Boolean = false) {
        if (!smoothScale) {
            viewState.setScale(scale, focusX, focusY)
            invalidate()
            return
        }

        scaling = Scaling(
            viewState.currentScale, scale,
            System.currentTimeMillis(), SCALING_DURATION,
            focusX, focusY
        )

        startAnimation()
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {

        Log.d(TAG, "onDoubleTapEvent")

        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.d(TAG, "onSingleTapConfirmed")

        return false
    }
}

private data class Scaling(
    val from: Float,
    val to: Float,
    val startTimeMillis: Long,
    val durationMillis: Long,
    val focusX: Float,
    val focusY: Float,
    val diff: Float = to - from
)
