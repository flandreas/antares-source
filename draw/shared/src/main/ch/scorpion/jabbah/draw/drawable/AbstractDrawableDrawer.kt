package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable

/**
 * Abstract base implementation of a [DrawableDrawer].
 */
abstract class AbstractDrawableDrawer<T : Drawable> : DrawableDrawer<T> {

    override var successor: DrawableDrawer<T>? = null

    /**
     * Must be called by subclasses after they have done their processing in order to proceed with the
     * successor of this [DrawableDrawer].
     */
    protected fun nextProcessor(context: DrawContext, drawable: T) {
        successor?.process(context, drawable)
    }
}