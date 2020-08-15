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
import androidx.core.view.ViewConfigurationCompat
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BookView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr),
    GestureDetector.OnGestureListener,
    ScaleGestureDetector.OnScaleGestureListener,
    Scrollable, GestureDetector.OnDoubleTapListener {

    companion object {
        private val TAG = BookView::class.java.simpleName
    }

    constructor(context: Context) : this(context, null, 0x0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0x0)

    private val viewConfiguration: ViewConfiguration = ViewConfiguration.get(context)

    override val scaledHorizontalScrollFactor: Float =
        ViewConfigurationCompat.getScaledHorizontalScrollFactor(viewConfiguration, context)
    override val scaledVerticalScrollFactor: Float =
        ViewConfigurationCompat.getScaledVerticalScrollFactor(viewConfiguration, context)

    override val maxFlingVelocity = viewConfiguration.scaledMaximumFlingVelocity
    override val minFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity

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

    private val viewState = ViewState().also {
        it.scrollable = this@BookView
    }

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

        viewState.scrollTo(
            rect.left.roundToInt(),
            rect.right.roundToInt(),
            smoothScroll = smoothScroll
        )
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

        return super.onTouchEvent(event)
    }

    private var scroller = OverScroller(context, DecelerateInterpolator())
    override fun scroller(): OverScroller = scroller

    override fun currentPageRect(): Rectangle? {
        val layoutManagerSnapshot = layoutManager ?: return null
        return layoutManagerSnapshot.currentPageRect(viewState)
    }

    override fun startScrollOrScale() {
        ViewCompat.postInvalidateOnAnimation(this);
    }

    override fun cancelScroll() {
        scroller.forceFinished(true)
    }

    private var scaleInterpolator = DecelerateInterpolator()

    private var scaling: Scaling? = null

    override fun startScale(
        fromScale: Float,
        toScale: Float,
        focusX: Float,
        focusY: Float,
        duration: Long
    ) {
        scaling = Scaling(
            fromScale, toScale,
            System.currentTimeMillis(), duration,
            focusX, focusY
        )

        startScrollOrScale()
    }

    override fun computeScroll() {
        super.computeScroll()

        if (scroller.computeScrollOffset()) {
            viewState.offsetTo(scroller.currX, scroller.currY)
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

    override fun onShowPress(e: MotionEvent?) = cancelScroll()

    override fun onSingleTapUp(e: MotionEvent?) = false

    override fun onDown(e: MotionEvent?) = true

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val layoutManagerSnapshot = layoutManager ?: return false
        Log.d(
            TAG,
            "onFling: ${velocityX}, ${velocityY} ${maxFlingVelocity}"
        )

        val velocityRatioX = velocityX / viewState.currentScale / maxFlingVelocity
        val velocityRatioY = velocityY / viewState.currentScale / maxFlingVelocity

        var targetPageRect =
            layoutManagerSnapshot.nextPageRect(viewState, velocityRatioX, velocityRatioY)
        if (targetPageRect == null) {
            targetPageRect = layoutManagerSnapshot.currentPageRect(viewState)
        }

        return viewState.onFling(velocityX, velocityY, targetPageRect)
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
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        e ?: return false

        Log.d(TAG, "onDoubleTap")

        viewState.scale(2.5F, e.x, e.y, smoothScale = true)
        return true
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
