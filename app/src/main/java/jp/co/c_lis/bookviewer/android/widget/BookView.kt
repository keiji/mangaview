package jp.co.c_lis.bookviewer.android.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class BookView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0x0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0x0)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return
    }
}
