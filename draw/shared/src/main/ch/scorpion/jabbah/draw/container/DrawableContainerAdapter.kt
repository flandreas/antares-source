package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.DrawableContainerListener

/**
 * Empty implementation of the [DrawableContainerListener] interface.
 */
open class DrawableContainerAdapter<T: Drawable> : DrawableContainerListener<T> {

    override fun drawableAdded(event: DrawableContainerEvent<T>) {
        // empty
    }

    override fun drawableRemoved(event: DrawableContainerEvent<T>) {
        // empty
    }
}