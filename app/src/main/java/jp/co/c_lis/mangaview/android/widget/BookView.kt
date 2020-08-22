package jp.co.c_lis.mangaview.android.widget

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
import jp.co.c_lis.mangaview.android.Log
import jp.co.c_lis.mangaview.android.Rectangle
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

        private const val SCROLLING_DURATION = 280
        private const val REVERSE_SCROLLING_DURATION = 350
        private const val SCALING_DURATION = 350L
    }

    constructor(context: Context) : this(context, null, 0x0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0x0)

    private val viewConfiguration: ViewConfiguration = ViewConfiguration.get(context)
    private val density = context.resources.displayMetrics.scaledDensity

    private val overScrollDistance =
        (viewConfiguration.scaledOverscrollDistance * density).roundToInt()
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

    var pageLayoutManager: PageLayoutManager = DoublePageLayoutManager(isSpread = true)
        set(value) {
            field = value
            isInitialized = false
            invalidate()
        }


    private val viewState = ViewState()

    private var isInitialized = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewState.setViewSize(w, h)
        layoutManager?.also {
            it.setViewSize(w, h)
            viewState.offsetTo(it.initialScrollX, it.initialScrollY)
        }

        isInitialized = false
    }

    private fun init() {
        val layoutManagerSnapshot = layoutManager ?: return
        val adapterSnapshot = adapter ?: return

        layoutManagerSnapshot.adapter = adapterSnapshot
        layoutManagerSnapshot.pageLayoutManager = pageLayoutManager
        pageLayoutManager.pageAdapter = adapterSnapshot

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

    private val visiblePages = ArrayList<PageLayout>()
    private val recycleBin = ArrayList<PageLayout>()

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

        visiblePages.forEach { pageLayout ->
            recycleBin.remove(pageLayout)

            pageLayout.pages.forEach { page ->
                if (!page.position.intersect(viewState.viewport)) {
                    return@forEach
                }

                if (!page.draw(canvas, viewState, paint, coroutineScope)) {
                    result = false
                }
            }
        }

        coroutineScope.launch(Dispatchers.Unconfined) {
            synchronized(recycleBin) {
                recycleBin.forEach { pageLayout ->
                    pageLayout.pages.forEach {
                        it.recycle()
                    }
                }
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

        val currentScrollArea = layoutManagerSnapshot.currentPageLayout(viewState)
            ?.calcScrollArea(tmpCurrentScrollArea, viewState.currentScale) ?: return

        layoutManagerSnapshot.populateHelper
            .init(
                viewState,
                layoutManagerSnapshot,
                settleScroller,
                pagingTouchSlop,
                SCROLLING_DURATION,
                REVERSE_SCROLLING_DURATION
            )
            .populateToCurrent(
                currentScrollArea, SCROLLING_DURATION
            )
        startAnimation()

        scalingState = ScalingState.Finish
    }

    private var settleScroller = OverScroller(context, DecelerateInterpolator())

    private var scroller = OverScroller(context, DecelerateInterpolator())

    private fun startAnimation() {
        ViewCompat.postInvalidateOnAnimation(this)
    }

    private fun abortAnimation() {
        scroller.abortAnimation()
        settleScroller.abortAnimation()
    }

    private var scaleInterpolator = DecelerateInterpolator()

    private var scaling: Scaling? = null

    override fun computeScroll() {
        super.computeScroll()

        if (!isInitialized) {
            return
        }

        var needPostInvalidateOnAnimation = false

        scaling?.also {
            val elapsed = System.currentTimeMillis() - it.startTimeMillis
            val input = elapsed.toFloat() / it.durationMillis
            val scaleFactor = scaleInterpolator.getInterpolation(input)
            val newScale = it.from + it.diff * scaleFactor
            viewState.scaleTo(newScale, it.focusX, it.focusY)

            if (input > 1.0F) {
                scaling = null
                needPostInvalidateOnAnimation = false
                populate()
            } else {
                needPostInvalidateOnAnimation = true
            }
        }

        if (scroller.isFinished && !settleScroller.isFinished && settleScroller.computeScrollOffset()) {
            // secondary
            viewState.offsetTo(settleScroller.currX.toFloat(), settleScroller.currY.toFloat())
            needPostInvalidateOnAnimation =
                needPostInvalidateOnAnimation || !settleScroller.isFinished

        } else if (!scroller.isFinished && scroller.computeScrollOffset()) {
            // primary
            viewState.offsetTo(scroller.currX.toFloat(), scroller.currY.toFloat())
            needPostInvalidateOnAnimation = needPostInvalidateOnAnimation || !scroller.isFinished
        }

        layoutManager?.currentPageLayout(viewState)?.also {
            currentPageLayout = it
        }

        if (needPostInvalidateOnAnimation) {
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

    var currentPageLayout: PageLayout? = null
        set(value) {
            if (field == value) {
                return
            }
            Log.d(TAG, "currentPageLayout has Changed. " + value?.position)
            field = value
        }

    private val tmpCurrentScrollArea = Rectangle()

    private fun fling(velocityX: Float, velocityY: Float): Boolean {
        val layoutManagerSnapshot = layoutManager ?: return false

        val scaledVelocityX = velocityX / viewState.currentScale
        val scaledVelocityY = velocityY / viewState.currentScale

        Log.d(TAG, "scaledVelocityX $scaledVelocityX")
        Log.d(TAG, "scaledVelocityY $scaledVelocityY")

        val currentPageLayoutSnapshot = currentPageLayout ?: return false

        val currentScrollArea =
            currentPageLayoutSnapshot.calcScrollArea(tmpCurrentScrollArea, viewState.currentScale)

        Log.d(TAG, "fling ${currentPageLayoutSnapshot.pages[0].index}")

        val populateHelper = layoutManagerSnapshot.populateHelper
            .init(
                viewState,
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
            val leftRect = layoutManagerSnapshot.leftPageLayout(viewState)
            val rightRect = layoutManagerSnapshot.rightPageLayout(viewState)

            handleHorizontal = if (scaledVelocityX > 0.0F && leftRect != null
                && !viewState.canScrollLeft(currentScrollArea)
            ) {
                Log.d(TAG, "populateToLeft")
                populateHelper.populateToLeft(leftRect)
                true
            } else if (scaledVelocityX < 0.0F && rightRect != null
                && !viewState.canScrollRight(currentScrollArea)
            ) {
                Log.d(TAG, "populateToRight")
                populateHelper.populateToRight(rightRect)
                true
            } else {
                false
            }
        } else {
            val topRect = layoutManagerSnapshot.topPageLayout(viewState)
            val bottomRect = layoutManagerSnapshot.bottomPageLayout(viewState)

            handleVertical = if (scaledVelocityY > 0.0F && topRect != null
                && !viewState.canScrollTop(currentScrollArea)
            ) {
                populateHelper.populateToTop(topRect)
                true
            } else if (scaledVelocityY < 0.0F && bottomRect != null
                && !viewState.canScrollBottom(currentScrollArea)
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

        val minX = currentScrollArea.left.roundToInt() - overScrollDistance
        val maxX =
            (currentScrollArea.right - viewState.scaledWidth).roundToInt() + overScrollDistance

        val minY = currentScrollArea.top.roundToInt() - overScrollDistance
        val maxY =
            (currentScrollArea.bottom - viewState.scaledHeight).roundToInt() + overScrollDistance

        Log.d(
            TAG, "fling " +
                    "currentX ${viewState.currentX}, currentY ${viewState.currentY}, " +
                    "scaledVelocityX ${scaledVelocityX}, scaledVelocityY ${scaledVelocityY}, " +
                    "minX ${minX}, maxX ${maxX}, " +
                    "minY ${minY}, maxY ${maxY}"
        )

        scroller.fling(
            viewState.currentX.roundToInt(),
            viewState.currentY.roundToInt(),
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
        val result = viewState.scroll(
            distanceX / viewState.currentScale,
            distanceY / viewState.currentScale
        )

        layoutManager?.currentPageLayout(viewState)?.also {
            currentPageLayout = it
        }

        return result
    }

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
