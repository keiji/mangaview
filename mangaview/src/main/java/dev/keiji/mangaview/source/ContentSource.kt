package dev.keiji.mangaview.source

import dev.keiji.mangaview.widget.ViewContext

abstract class ContentSource {

    /**
     * The state of the content source.
     */
    enum class State {
        NA,
        Preparing,
        Prepared,
    }

    /**
     * The width property of the content.
     */
    abstract val contentWidth: Float

    /**
     * The height property of the content.
     */
    abstract val contentHeight: Float

    /**
     * Gets the state of the content source.
     *
     * @param viewContext The ViewContext the MangaView is running in
     *
     * @return The state of the content source: one of `State.NA`, `State.Preparing` or `State.Prepared`.
     */
    abstract fun getState(viewContext: ViewContext): State

    /**
     * Prepare the content source.
     *
     * @param viewContext
     * @param onPrepared
     *
     * @return `true` the content is prepared,
     *         `false` otherwise
     */
    abstract fun prepare(viewContext: ViewContext, onPrepared: () -> Unit): Boolean

    /**
     * Free objects associated with this source.
     */
    abstract fun recycle()
}
