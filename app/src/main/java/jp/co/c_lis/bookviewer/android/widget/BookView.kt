package jp.co.c_lis.bookviewer.android.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ScaleGestureDetectorCompat
import androidx.core.view.ViewCompat
import jp.co.c_lis.bookviewer.android.Log
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.roundToInt

class BookView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr),
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener,
    ScaleGestureDetector.OnScaleGestureListener {

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

    var pageLayoutManager: PageLayoutManager = DoublePageLayoutManager(true)
        set(value) {
            field = value
            isInitialized = false
            invalidate()
        }


    private val viewState = ViewState()

    private var isInitialized = false

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
        layoutManagerSnapshot.layout(viewState, pageLayoutManager)

        isInitialized = true
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var paint = Paint().also {
        it.isAntiAlias = true
        it.isDither = true
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var coroutineScope = CoroutineScope(Dispatchers.Main)
        set(value) {
            field.cancel()
            field = value
        }

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

    fun showPage(rect: Rectangle, smoothScroll: Boolean = false) {
        if (!smoothScroll) {
            viewState.offsetTo(rect.left, rect.top)
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
            invalidate()
        }

        if (gestureDetector.onTouchEvent(event)) {
            invalidate()
            return true
        }

        return super.onTouchEvent(event)
    }

    private fun populate() {
        val layoutManagerSnapshot = layoutManager ?: return

        val touchSlop = if (scalingState == ScalingState.End) {
            Float.MAX_VALUE
        } else {
            pagingTouchSlop
        }

        layoutManagerSnapshot.populateHelper
            .init(
                viewState,
                layoutManagerSnapshot,
                settleScroller,
                touchSlop,
                SCROLLING_DURATION
            )
            .populate()
        startAnimation()

        scalingState = ScalingState.Finish
    }

    private var settleScroller = OverScroller(context, DecelerateInterpolator())
    private var scroller = OverScroller(context, DecelerateInterpolator())

    private fun startAnimation() {
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private fun abortAnimation() {
        scroller.abortAnimation()
        settleScroller.abortAnimation()
    }

    private var scaleInterpolator = DecelerateInterpolator()

    private var scaling: Scaling? = null

    override fun computeScroll() {
        super.computeScroll()

        if (!scroller.isFinished && scroller.computeScrollOffset()) {
            viewState.offsetTo(scroller.currX.toFloat(), scroller.currY.toFloat())
            ViewCompat.postInvalidateOnAnimation(this)
        }

        if (!settleScroller.isFinished && settleScroller.computeScrollOffset()) {
            viewState.offsetTo(settleScroller.currX.toFloat(), settleScroller.currY.toFloat())
            ViewCompat.postInvalidateOnAnimation(this)
        }

        scaling?.also {
            val elapsed = System.currentTimeMillis() - it.startTimeMillis
            val input = elapsed.toFloat() / it.durationMillis
            val scaleFactor = scaleInterpolator.getInterpolation(input)
            val newScale = it.from + it.diff * scaleFactor
            viewState.scaleTo(newScale, it.focusX, it.focusY)

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
    ): Boolean {
        val handled = fling(velocityX, velocityY)
        if (handled) {
            startAnimation()
        }
        return handled
    }

    private fun fling(velocityX: Float, velocityY: Float): Boolean {
        val layoutManagerSnapshot = layoutManager ?: return false

        val scaledVelocityX = velocityX / viewState.currentScale
        val scaledVelocityY = velocityY / viewState.currentScale

        Log.d(TAG, "scaledVelocityX $scaledVelocityX")
        Log.d(TAG, "scaledVelocityY $scaledVelocityY")

        val currentRect = layoutManagerSnapshot.currentPageLayout(viewState).position

        val populateHelper = layoutManagerSnapshot.populateHelper
            .init(
                viewState,
                layoutManagerSnapshot,
                settleScroller,
                pagingTouchSlop,
                SCROLLING_DURATION
            )

        if (abs(scaledVelocityX) > abs(scaledVelocityY)) {
            // horizontal
            if (scaledVelocityX > 0.0F && !viewState.canScrollLeft(currentRect)) {
                // left
                Log.d(TAG, "left Page")
                val leftRect = layoutManagerSnapshot.leftPageLayout(viewState)
                leftRect ?: return false
                populateHelper.populateToLeft(leftRect)
                return true
            } else if (scaledVelocityX < 0.0F && !viewState.canScrollRight(currentRect)
            ) {
                // right
                Log.d(TAG, "right Page")
                val rightRect = layoutManagerSnapshot.rightPageLayout(viewState)
                rightRect ?: return false
                populateHelper.populateToRight(rightRect)
                return true
            }
        } else {
            // vertical
            if (scaledVelocityY > 0.0F && !viewState.canScrollTop(currentRect)) {
                // top
                Log.d(TAG, "top Page")
                val topRect = layoutManagerSnapshot.topPageLayout(viewState)
                topRect ?: return false
                populateHelper.populateToTop(topRect)
                return true
            } else if (scaledVelocityY < 0.0F && !viewState.canScrollBottom(currentRect)
            ) {
                // bottom
                Log.d(TAG, "bottom Page")
                val bottomRect = layoutManagerSnapshot.bottomPageLayout(viewState)
                bottomRect ?: return false
                populateHelper.populateToBottom(bottomRect)
                return true
            }
        }

        val minX = currentRect.left.roundToInt()
        val maxX = (currentRect.right - viewState.scaledWidth).roundToInt()

        val minY = currentRect.top.roundToInt()
        val maxY = (currentRect.bottom - viewState.scaledHeight).roundToInt()

        scroller.fling(
            viewState.currentX.roundToInt(),
            viewState.currentY.roundToInt(),
            -scaledVelocityX.roundToInt(),
            -scaledVelocityY.roundToInt(),
            minX, maxX,
            minY, maxY
        )

        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean = viewState.scroll(
        distanceX / viewState.currentScale,
        distanceY / viewState.currentScale
    )

    override fun onLongPress(e: MotionEvent?) {
        Log.d(TAG, "onLongPress")
    }

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

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        detector ?: return false

        scalingState = ScalingState.Begin

        return true
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        detector ?: return false

        scalingState = ScalingState.Scaling
        return viewState.scale(detector.scaleFactor, detector.focusX, detector.focusY)
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        detector ?: return

        scalingState = ScalingState.End
    }

    private fun scale(scale: Float, focusX: Float, focusY: Float, smoothScale: Boolean = false) {
        if (!smoothScale) {
            viewState.scaleTo(scale, focusX, focusY)
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

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        e ?: return false

        Log.d(TAG, "onDoubleTap")

        scale(2.5F, e.x, e.y, smoothScale = true)
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        e ?: return false

        Log.d(TAG, "onDoubleTapEvent ${e.action}")

        when (e.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_UP -> {
                abortAnimation()
            }
        }
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.d(TAG, "onSingleTapConfirmed")

        return false
    }

    private data class Scaling(
        val from: Float,
        val to: Float,
        val startTimeMillis: Long,
        val durationMillis: Long,
        val focusX: Float,
        val focusY: Float
    ) {
        val diff: Float = to - from
    }
}
