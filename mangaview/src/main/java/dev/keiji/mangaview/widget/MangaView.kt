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
import dev.keiji.mangaview.Config
import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle
import dev.keiji.mangaview.layer.ContentLayer
import kotlin.math.abs
import kotlin.math.roundToInt

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

        /**
         * The view is not scrolling.
         */
        const val SCROLL_STATE_IDLE = 0

        /**
         * The user is touching view and scrolling.
         */
        const val SCROLL_STATE_DRAGGING = 1

        /**
         * The view is moving between pages.
         */
        const val SCROLL_STATE_SETTLING = 2

        private const val SCROLLING_DURATION = 280L
        private const val SCALING_DURATION = 250L

        private const val FOCUS_DURATION = 220L
    }

    constructor(context: Context) : this(context, null, 0x0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0x0)

    private var scrollState: Int = SCROLL_STATE_IDLE
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

    /**
     * Adds the listener to the list of listeners who will be notified when the user tap on view.
     */
    fun addOnTapListener(onTapListener: OnTapListener) {
        if (onTapListenerList.contains(onTapListener)) {
            return
        }
        onTapListenerList.add(onTapListener)
    }

    /**
     * Remove the listener to the list of listeners.
     */
    fun removeOnTapListener(onTapListener: OnTapListener) {
        onTapListenerList.remove(onTapListener)
    }

    private val onPageChangeListenerList = ArrayList<OnPageChangeListener>()

    /**
     * Adds the listener to the list of listeners who will be notified when
     * page transition has been occurred.
     */
    fun addOnPageChangeListener(onPageChangeListener: OnPageChangeListener) {
        if (onPageChangeListenerList.contains(onPageChangeListener)) {
            return
        }
        onPageChangeListenerList.add(onPageChangeListener)
    }

    /**
     * Remove the listener to the list of listeners.
     */
    fun removeOnPageChangeListener(onPageChangeListener: OnPageChangeListener) {
        onPageChangeListenerList.add(onPageChangeListener)
    }

    private val onReadCompleteListenerList = ArrayList<OnReadCompleteListener>()

    /**
     * Adds the listener to the list of listeners who will be notified when
     * page transition to next have been occurred on last page by the user.
     */
    fun addOnReadCompleteListener(onReadCompleteListener: OnReadCompleteListener) {
        if (onReadCompleteListenerList.contains(onReadCompleteListener)) {
            return
        }
        onReadCompleteListenerList.add(onReadCompleteListener)
    }

    /**
     * Remove the listener to the list of listeners.
     */
    fun removeOnReadCompleteListener(onReadCompleteListener: OnReadCompleteListener) {
        onReadCompleteListenerList.remove(onReadCompleteListener)
    }

    private val onDoubleTapListenerList = ArrayList<OnDoubleTapListener>()

    /**
     * Adds the listener to the list of listeners who will be notified when the user double tap on view.
     */
    fun addOnDoubleTapListener(onDoubleTapListener: OnDoubleTapListener) {
        if (onDoubleTapListenerList.contains(onDoubleTapListener)) {
            return
        }
        onDoubleTapListenerList.add(onDoubleTapListener)
    }

    /**
     * Remove the listener to the list of listeners.
     */
    fun removeOnDoubleTapListener(onDoubleTapListener: OnDoubleTapListener) {
        onDoubleTapListenerList.remove(onDoubleTapListener)
    }

    private val onLongTapListenerList = ArrayList<OnLongTapListener>()

    /**
     * Adds the listener to the list of listeners who will be notified when the user long tap on view.
     */
    fun addOnLongTapListener(onLongTapListener: OnLongTapListener) {
        if (onLongTapListenerList.contains(onLongTapListener)) {
            return
        }
        onLongTapListenerList.add(onLongTapListener)
    }

    /**
     * Remove the listener to the list of listeners.
     */
    fun removeOnLongTapListener(onLongTapListener: OnLongTapListener) {
        onLongTapListenerList.remove(onLongTapListener)
    }

    private var onContentViewportChangeListenerList = ArrayList<OnContentViewportChangeListener>()

    /**
     * Adds the listener to the list of listeners who will be notified when
     * the viewport has been changed on content by the user.
     */
    fun addOnContentViewportChangeListener(onContentViewportChangeListener: OnContentViewportChangeListener) {
        if (onContentViewportChangeListenerList.contains(onContentViewportChangeListener)) {
            return
        }
        onContentViewportChangeListenerList.add(onContentViewportChangeListener)
    }

    /**
     * Remove the listener to the list of listeners.
     */
    fun removeOnContentViewportChangeListener(onContentViewportChangeListener: OnContentViewportChangeListener) {
        onContentViewportChangeListenerList.remove(onContentViewportChangeListener)
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

    var config = Config()

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

    var currentPageIndex: Int = 0

    private val tmpCurrentScrollArea = Rectangle()
    private val tmpEventPoint = Rectangle()

    private val currentScrollableArea: Rectangle?
        get() {
            return currentPageLayout?.calcScrollArea(viewContext, tmpCurrentScrollArea)
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

        isInitialized = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!isInitialized) {
            init()
            showInitialPage(currentPageIndex)
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
                    onContentViewportChangeListenerList.forEach {
                        it.onViewportChanged(this, layer, viewport)
                    }
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

    private fun showInitialPage(pageIndex: Int) {
        val pageLayoutIndex = pageLayoutManager.calcPageLayoutIndex(pageIndex)
        val pageLayout = layoutManager?.getPageLayout(pageLayoutIndex, viewContext)

        if (pageLayout == null) {
            Log.d(TAG, "pageIndex: ${pageIndex} -> pageLayoutIndex ${pageLayoutIndex} not found.")
            return
        }

        scale(
            viewContext.minScale,
            viewContext.viewport.centerY,
            viewContext.viewport.centerY,
            smoothScale = false
        )

        val position = pageLayout.globalPosition
        viewContext.offsetTo(position.left, position.top)
        layoutManager?.obtainVisiblePageLayout(viewContext, visiblePageLayoutList)

        postInvalidate()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun showPage(pageIndex: Int, smoothScroll: Boolean) {

        val pageLayoutIndex = pageLayoutManager.calcPageLayoutIndex(pageIndex)
        val pageLayout = layoutManager?.getPageLayout(pageLayoutIndex, viewContext)

        if (pageLayout == null) {
            Log.d(TAG, "pageIndex: ${pageIndex} -> pageLayoutIndex ${pageLayoutIndex} not found.")
            return
        }

        val duration = if (!smoothScroll) {
            0L
        } else {
            SCROLLING_DURATION
        }

        animator = Animator().populateTo(
            viewContext,
            pageLayout,
            durationMillis = duration
        )
        scrollState = SCROLL_STATE_SETTLING
        startAnimation()
    }

    fun focus(globalRegion: Rectangle, duration: Long = FOCUS_DURATION) {
        animator = Animator().focus(viewContext, currentPageLayout, globalRegion, duration)
        postInvalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                abortAnimation()
                scrollState = SCROLL_STATE_IDLE
            }
            MotionEvent.ACTION_UP -> {
                populateToCurrent()
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

    private fun populateToCurrent() {
        val layoutManagerSnapshot = layoutManager ?: return

        val lastPageLayout = layoutManagerSnapshot.lastPageLayout(viewContext) ?: return
        if (currentPageLayout == lastPageLayout) {
            handleReadCompleteEvent()
        }

        animator = layoutManagerSnapshot.populateHelper
            .init(
                viewContext,
                layoutManagerSnapshot,
                pagingTouchSlop,
                SCROLLING_DURATION
            )
            .populateToCurrent(currentPageLayout)

        startAnimation()

        scalingState = ScalingState.Finish
    }

    private fun handleReadCompleteEvent(): Boolean {
        val layoutManagerSnapshot = layoutManager ?: return false
        val currentScrollableAreaSnapshot = currentScrollableArea ?: return false
        val viewport = viewContext.viewport

        if (layoutManagerSnapshot.leftPageLayout(viewContext) == null
            && viewport.left < currentScrollableAreaSnapshot.left
        ) {
            return fireEventReadComplete(
                abs(currentScrollableAreaSnapshot.left - viewport.left)
            )
        }

        if (layoutManagerSnapshot.rightPageLayout(viewContext) == null
            && viewport.right > currentScrollableAreaSnapshot.right
        ) {
            return fireEventReadComplete(
                abs(viewport.right - currentScrollableAreaSnapshot.right)
            )
        }

        if (layoutManagerSnapshot.topPageLayout(viewContext) == null
            && viewport.top < currentScrollableAreaSnapshot.top
        ) {
            return fireEventReadComplete(
                abs(currentScrollableAreaSnapshot.top - viewport.top)
            )
        }

        if (layoutManagerSnapshot.bottomPageLayout(viewContext) == null
            && viewport.bottom > currentScrollableAreaSnapshot.bottom
        ) {
            return fireEventReadComplete(
                abs(viewport.bottom - currentScrollableAreaSnapshot.bottom)
            )
        }
        return false
    }

    internal fun fireEventReadComplete(overScroll: Float = pagingTouchSlop): Boolean {
        if (overScroll < pagingTouchSlop) {
            return false
        }

        onReadCompleteListenerList.forEach {
            it.onReadCompleted(this)
        }

        return true
    }

    private fun startAnimation() {
        ViewCompat.postInvalidateOnAnimation(this)
    }

    private fun abortAnimation() {
        scroller.abortAnimation()
        animation = null
        animator = null
    }

    override fun computeScroll() {
        super.computeScroll()

        if (!isInitialized) {
            return
        }

        val animatorSnapshot = animator

        // Scroller first
        val needPostInvalidate = if (animatorSnapshot != null) {
            animatorSnapshot.let {
                scrollState = SCROLL_STATE_SETTLING
                if (it.computeAnimation(viewContext)) {
                    return@let true
                } else {
                    animator = null
                    scrollState = SCROLL_STATE_IDLE
                    return@let false
                }
            }
        } else {
            false
        }

        if (!scroller.isFinished && scroller.computeScrollOffset()) {
            viewContext.offsetTo(scroller.currX.toFloat(), scroller.currY.toFloat())
            if (scroller.isFinished) {
                populateToCurrent()
            }
        }

        val needPostInvalidateScroll = !scroller.isFinished || needPostInvalidate

        if (!needPostInvalidateScroll && scrollState == SCROLL_STATE_SETTLING) {
            scrollState = SCROLL_STATE_IDLE
        }

        if (needPostInvalidateScroll) {
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

    private fun handleOnTapListener(
        x: Float,
        y: Float,
        globalPosition: Rectangle,
        onTapListenerList: List<OnTapListener>
    ) {
        visiblePageLayoutList
            .flatMap { it.pages }
            .forEach pageLoop@{ page ->
                page.layers.forEach { layer ->
                    if (layer.requestHandleOnTapEvent(
                            globalPosition.centerX,
                            globalPosition.centerY,
                            onTapListenerList
                        )
                    ) {
                        return
                    }
                }

                if (page.requestHandleOnTapEvent(
                        globalPosition.centerX,
                        globalPosition.centerY,
                        onTapListenerList
                    )
                ) {
                    return
                }

            }

        onTapListenerList.forEach {
            if (it.onTap(this, x, y)) {
                return
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
        if (!handled) {
            populateToCurrent()
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
                pagingTouchSlop,
                SCROLLING_DURATION
            )

        val horizontal = (abs(scaledVelocityX) > abs(scaledVelocityY))

        val scale = if (config.resetScaleOnPageChanged) {
            viewContext.minScale
        } else {
            viewContext.currentScale
        }

        val populateAnimation = if (horizontal) {
            val leftPageLayout =
                layoutManagerSnapshot.leftPageLayout(viewContext, currentPageLayout)
            val rightPageLayout =
                layoutManagerSnapshot.rightPageLayout(viewContext, currentPageLayout)

            if (scaledVelocityX > 0.0F && leftPageLayout != null
                && !viewContext.canScrollLeft(currentScrollAreaSnapshot)
            ) {
                populateHelper.populateToLeft(leftPageLayout, scale)
            } else if (scaledVelocityX < 0.0F && rightPageLayout != null
                && !viewContext.canScrollRight(currentScrollAreaSnapshot)
            ) {
                populateHelper.populateToRight(rightPageLayout, scale)
            } else {
                null
            }
        } else {
            val topPageLayout = layoutManagerSnapshot.topPageLayout(viewContext, currentPageLayout)
            val bottomPageLayout =
                layoutManagerSnapshot.bottomPageLayout(viewContext, currentPageLayout)

            if (scaledVelocityY > 0.0F && topPageLayout != null
                && !viewContext.canScrollTop(currentScrollAreaSnapshot)
            ) {
                populateHelper.populateToTop(topPageLayout, scale)
            } else if (scaledVelocityY < 0.0F && bottomPageLayout != null
                && !viewContext.canScrollBottom(currentScrollAreaSnapshot)
            ) {
                populateHelper.populateToBottom(bottomPageLayout, scale)
            } else {
                null
            }
        }

        if (populateAnimation != null) {
            scrollState = SCROLL_STATE_SETTLING
            startAnimation()
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
        e ?: return

        // Global coordinate projection
        val globalPoint = viewContext.projectionGlobalPoint(e.x, e.y, tmpEventPoint)

        handleOnLongTap(e.x, e.y, globalPoint, onLongTapListenerList)

        postInvalidate()
    }

    private fun handleOnLongTap(
        x: Float,
        y: Float,
        globalPosition: Rectangle,
        onLongTapListenerList: List<OnLongTapListener>
    ) {
        visiblePageLayoutList
            .flatMap { it.pages }
            .forEach pageLoop@{ page ->
                page.layers.forEach { layer ->
                    if (layer.requestHandleOnLongTapEvent(
                            globalPosition.centerX,
                            globalPosition.centerY,
                            onLongTapListenerList
                        )
                    ) {
                        return
                    }
                }

                if (page.requestHandleOnLongTapEvent(
                        globalPosition.centerX,
                        globalPosition.centerY,
                        onLongTapListenerList
                    )
                ) {
                    return
                }

            }

        onLongTapListenerList.forEach {
            if (it.onLongTap(this, x, y)) {
                return
            }
        }
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
        viewContext.scale(
            detector.scaleFactor,
            detector.focusX, detector.focusY,
            currentScrollableArea
        )

        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
        detector ?: return

        scalingState = ScalingState.End
    }

    private var animator: Animator? = null
        set(value) {
            if (value == null) {
                field = null
            }

            // scroller first
            if (!scroller.isFinished) {
                return
            }

            field = value
        }

    internal fun scale(
        scale: Float,
        focusOnViewX: Float,
        focusOnViewY: Float,
        smoothScale: Boolean = false,
    ) {
        val duration = if (!smoothScale) {
            0L
        } else {
            SCALING_DURATION
        }

        animator = Animator().scale(
            viewContext,
            currentPageLayout,
            scale,
            focusOnViewX,
            focusOnViewY,
            durationMillis = duration
        )
        startAnimation()
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        e ?: return false

        return true
    }

    private fun handleOnDoubleTapListener(
        x: Float,
        y: Float,
        globalPosition: Rectangle,
        onDoubleTapListenerList: List<OnDoubleTapListener>
    ) {
        visiblePageLayoutList
            .flatMap { it.pages }
            .forEach { page ->
                page.layers.forEach { layer ->
                    if (layer.requestHandleOnDoubleTapEvent(
                            globalPosition.centerX,
                            globalPosition.centerY,
                            onDoubleTapListenerList
                        )
                    ) {
                        return
                    }
                }

                if (page.requestHandleOnDoubleTapEvent(
                        globalPosition.centerX,
                        globalPosition.centerY,
                        onDoubleTapListenerList
                    )
                ) {
                    return
                }
            }

        onDoubleTapListenerList.forEach {
            if (it.onDoubleTap(this, x, y)) {
                return
            }
        }
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        e ?: return false

        if (e.action == MotionEvent.ACTION_UP) {
            // Global coordinate projection
            val globalPoint = viewContext.projectionGlobalPoint(e.x, e.y, tmpEventPoint)

            handleOnDoubleTapListener(e.x, e.y, globalPoint, onDoubleTapListenerList)
        }

        postInvalidate()

        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        e ?: return false

        // Global coordinate projection
        val globalPoint = viewContext.projectionGlobalPoint(e.x, e.y, tmpEventPoint)

        handleOnTapListener(e.x, e.y, globalPoint, onTapListenerList)

        postInvalidate()

        return true
    }

    /**
     * Interface definition for a callback to be invoked when a MangaView is tapped.
     */
    interface OnTapListener {
        /**
         * Handle a tap event by the MangaView.
         *
         * @param mangaView  The MangaView that handled event
         * @param viewX The point X on View
         * @param viewY The point Y on View
         *
         * @return True if the event was consumed by the view, false otherwise.
         */
        fun onTap(mangaView: MangaView, viewX: Float, viewY: Float): Boolean = false

        /**
         * Handle a tap event by the Page.
         *
         * @param page The Page that handled event
         * @param pageX The point X on Page
         * @param pageY The point Y on Page
         *
         * @return True if the event was consumed by the page, false otherwise.
         */
        fun onTap(page: Page, pageX: Float, pageY: Float): Boolean = false

        /**
         * Handle a tap event by the ContentLayer.
         *
         * @param layer The ContentLayer that handled event
         * @param contentX The point X on Content
         * @param contentY The point Y on Content
         *
         * @return True if the event was consumed by the contentLayer, false otherwise.
         */
        fun onTap(layer: ContentLayer, contentX: Float, contentY: Float): Boolean = false
    }

    /**
     * Interface definition for a callback to be invoked when a MangaView is double tapped.
     */
    interface OnDoubleTapListener {
        /**
         * Handle a double tap event by the MangaView
         *
         * @param mangaView The MangaView that handled event.
         * @param viewX The point X on View
         * @param viewY The point Y on View
         *
         * @return True if the event was consumed by the view, false otherwise.
         */
        fun onDoubleTap(mangaView: MangaView, viewX: Float, viewY: Float): Boolean = false

        /**
         * Handle a double tap event by the Page.
         *
         * @param page The Page that handled event
         * @param pageX The point X on Page
         * @param pageY The point Y on Page
         *
         * @return True if the event was consumed by the page, false otherwise.
         */
        fun onDoubleTap(page: Page, pageX: Float, pageY: Float): Boolean = false

        /**
         * Handle a double tap event by the ContentLayer.
         *
         * @param layer The ContentLayer that handled event.
         * @param contentX The point X on Content
         * @param contentY The point Y on Content
         *
         * @return True if the event was consumed by the contentLayer, false otherwise.
         */
        fun onDoubleTap(layer: ContentLayer, contentX: Float, contentY: Float): Boolean = false
    }

    /**
     * Interface definition for a callback to be invoked when a MangaView is long tapped.
     */
    interface OnLongTapListener {
        /**
         * Handle a long tap event by the MangaView.
         *
         * @param mangaView The MangaView that handled event
         * @param viewX The point X on View
         * @param viewY The point Y on View
         *
         * @return True if the event was consumed by the view, false otherwise.
         */
        fun onLongTap(mangaView: MangaView, viewX: Float, viewY: Float): Boolean = false

        /**
         * Handle a long tap event by the Page.
         *
         * @param page The Page that handled event.
         * @param pageX The point X on Page
         * @param pageY The point Y on Page
         *
         * @return True if the event was consumed by the page, false otherwise.
         */
        fun onLongTap(page: Page, pageX: Float, pageY: Float): Boolean = false

        /**
         * Handle a long tap event by the ContentLayer.
         *
         * @param layer The ContentLayer that handled event.
         * @param contentX The point X on Content
         * @param contentY The point Y on Content
         *
         * @return True if the event was consumed by the contentLayer, false otherwise.
         */
        fun onLongTap(layer: ContentLayer, contentX: Float, contentY: Float): Boolean = false
    }

    /**
     * Interface definition for a callback to be invoked when a page state is changed.
     */
    interface OnPageChangeListener {
        /**
         * Called when a scroll state has been changed.
         *
         * @param mangaView The MangaView that handled event
         * @param scrollState Constant MangaView.SCROLL_STATE_*
         */
        fun onScrollStateChanged(mangaView: MangaView, scrollState: Int) {}

        /**
         * Called when a PageLayout has been selected.
         *
         * @param mangaView The MangaView that handled event
         * @param pageLayout The selected PageLayout.
         */
        fun onPageLayoutSelected(mangaView: MangaView, pageLayout: PageLayout) {}
    }

    /**
     * Interface definition for a callback to be invoked when reading is completed.
     */
    interface OnReadCompleteListener {
        /**
         * Called when reading has been completed.
         *
         * @param mangaView The MangaView that handled event
         */
        fun onReadCompleted(mangaView: MangaView)
    }

    /**
     * Interface definition for a callback to be invoked when a viewport is changed.
     */
    interface OnContentViewportChangeListener {
        /**
         * Called when a viewport has been changed by contentLayer.
         *
         * @param mangaView The MangaView that handled event
         * @param layer
         * @param viewport
         */
        fun onViewportChanged(mangaView: MangaView, layer: ContentLayer, viewport: RectF) = false
    }
}
