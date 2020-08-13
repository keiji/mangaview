package jp.co.c_lis.bookviewer.android.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ScaleGestureDetectorCompat
import jp.co.c_lis.bookviewer.android.Rectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.lang.Math.abs
import java.lang.Math.min

class BookView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = BookView::class.java.simpleName
    }

    constructor(context: Context) : this(context, null, 0x0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0x0)

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
        }
        isInitialized = false
    }

    private fun init() {
        val adapterSnapshot = adapter ?: return
        val layoutManagerSnapshot = layoutManager ?: return

        layoutManagerSnapshot.pageList = (0 until adapterSnapshot.getPageCount())
            .map { adapterSnapshot.getPage(it) }

        layoutManagerSnapshot.layout(viewState)
        isInitialized = true
    }

    private val viewState = ViewState()

    private var isInitialized = false

    private val paint = Paint()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!isInitialized) {
            init()
            invalidate()
            return
        }

        layoutManager?.pageList?.forEach { page ->
            if (!page.position.intersect(viewState.viewport)) {
                Log.d(TAG, "number: ${page.number}, position: ${page.position} skipped.")
                return@forEach
            }

            if (!page.draw(canvas, viewState, paint, coroutineScope)) {
                invalidate()
                return
            }
        }
    }

    private val gestureDetector = GestureDetectorCompat(context, viewState)
    private val scaleGestureDetector = ScaleGestureDetector(context, viewState).also {
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
}
