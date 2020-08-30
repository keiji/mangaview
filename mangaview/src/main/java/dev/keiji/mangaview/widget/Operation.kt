package dev.keiji.mangaview.widget

data class Operation(
    var translate: Translate? = null,
    var scale: Scale? = null,
    val startTimeMillis: Long,
    val durationMillis: Long,
    val priority: Int = 0,
    val onOperationEnd: () -> Unit = {}
) {
    val elapsed: Long
        get() = System.currentTimeMillis() - startTimeMillis

    val isFinished: Boolean
        get() = translate == null && scale == null

    data class Scale(
        val from: Float,
        val to: Float,
        val focusX: Float?,
        val focusY: Float?
    ) {
        val diff: Float = to - from
    }

    data class Translate(
        val startX: Float,
        val startY: Float,
        val destX: Float,
        val destY: Float
    ) {
        val diffX: Float = destX - startX
        val diffY: Float = destY - startY

    }
}
