package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.io.Storable

/**
 * A storable [DrawableContainer] that contains [Component]s.
 */
interface ComponentContainer<T : Component> : DrawableContainer<T>, Storable {

    /** Returns the [Component] with the specified identification, if present.*/
    fun getWithId(id: Int): T?
}