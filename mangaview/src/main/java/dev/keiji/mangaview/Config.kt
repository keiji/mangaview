package dev.keiji.mangaview

private const val SCROLLING_DURATION_NORMAL = 280L
private const val SCALING_DURATION_NORMAL = 250L

private const val SCROLLING_DURATION_EINK = 0L
private const val SCALING_DURATION_EINK = 0L

enum class Mode(val scrollingDuration: Long, val scalingDuration: Long) {
    Normal(SCROLLING_DURATION_NORMAL, SCALING_DURATION_NORMAL),
    EInk(SCROLLING_DURATION_EINK, SCALING_DURATION_EINK),
}

data class Config(
    val mode: Mode = Mode.Normal,
    val resetScaleOnPageChanged: Boolean = false,
)
