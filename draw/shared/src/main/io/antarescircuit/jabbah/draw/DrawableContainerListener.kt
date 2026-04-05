package io.antarescircuit.jabbah.draw

/**
 * Listens for [DrawableContainerEvent]s from a [DrawableContainer].
 */
interface DrawableContainerListener<T : Drawable> {

    /** Notifies this [DrawableContainerListener] that a [Drawable] has been added to a observed [DrawableContainer]. */
    fun drawableAdded(event: DrawableContainerEvent<T>)

    /** Notifies this [DrawableContainerListener] that a [Drawable] has been removed from a observed [DrawableContainer].*/
    fun drawableRemoved(event: DrawableContainerEvent<T>)
}