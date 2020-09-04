package dev.keiji.mangaview.widget

import dev.keiji.mangaview.Log
import dev.keiji.mangaview.Rectangle
import kotlin.math.min

class FocusHelper() {
    companion object {
        private val TAG = FocusHelper::class.java.simpleName
    }

    fun focus(viewContext: ViewContext, focusRect: Rectangle): Animation {
        val scale = min(
            viewContext.viewWidth / focusRect.width,
            viewContext.viewHeight / focusRect.height
        )

        Log.d(TAG, "focusRect", focusRect)
        Log.d(TAG, "scale $scale")

        val scaledWidth = focusRect.width * scale
        val scaledHeight = focusRect.height * scale

        val paddingHorizontal = viewContext.viewWidth - scaledWidth
        val paddingVertical = viewContext.viewHeight - scaledHeight

        val paddingLeft = paddingHorizontal / 2
        val paddingTop = paddingVertical / 2

        val destX = focusRect.left - paddingLeft / scale
        val destY = focusRect.top - paddingTop / scale

        val animation = Animation(
            translate = Animation.Translate(
                viewContext.currentX, viewContext.currentY,
                destX, destY
            ),
            scale = Animation.Scale(
                viewContext.currentScale,
                scale,
                null,
                null
            ),
            durationMillis = 200,
            priority = -1,
            applyImmediatelyEachAnimation = true
        )

        return animation
    }
}
