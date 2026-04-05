package io.antarescircuit.jabbah.draw.container

import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawableContainerEvent
import io.antarescircuit.jabbah.draw.DrawableContainerListener

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