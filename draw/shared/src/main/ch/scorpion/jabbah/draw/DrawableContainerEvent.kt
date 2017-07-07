package ch.scorpion.jabbah.draw

/**
 * Sent by [DrawableContainer]s to their registered [DrawableContainerListener]s when [Drawable]s
 * have been added or removed.
 *
 * @param T the type of [Drawable] contained in the source [DrawableContainer]
 */
data class DrawableContainerEvent<T : Drawable>(val container: DrawableContainer<T>, val child: Drawable)