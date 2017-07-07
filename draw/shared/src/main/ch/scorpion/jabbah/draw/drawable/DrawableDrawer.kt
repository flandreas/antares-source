package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable

/**
 * An object that can be part of the process of drawing an individual [Drawable].
 * [DrawableDrawer]s form a chain of responsibility.
 */
interface DrawableDrawer<T: Drawable> {

    var successor: DrawableDrawer<T>?

    /**
     * Processes the specified [Drawable] in the drawing process.
     * After having processed the specified [Drawable], this [DrawableDrawer] must call
     * [process] of its successor, if any.
     */
    fun process(context: DrawContext, drawable: T)
}