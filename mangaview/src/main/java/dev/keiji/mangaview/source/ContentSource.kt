package dev.keiji.mangaview.source

import dev.keiji.mangaview.widget.ViewContext

abstract class ContentSource {

    enum class State {
        NA,
        Preparing,
        Prepared,
    }

    abstract val contentWidth: Float
    abstract val contentHeight: Float

    abstract fun getState(viewContext: ViewContext): State

    abstract fun prepare(viewContext: ViewContext, onPrepared: () -> Unit): Boolean

    abstract fun recycle()
}
