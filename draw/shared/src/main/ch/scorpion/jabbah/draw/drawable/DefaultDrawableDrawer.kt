package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable

/**
 * A default implementation of the [DrawableDrawer] interface that simply calls the drawing method
 * of a [Drawable].
 */
class DefaultDrawableDrawer<T : Drawable> : AbstractDrawableDrawer<T>() {

    override fun process(context: DrawContext, drawable: T) {
        drawable.draw(context)
        processDone(context, drawable)
    }
}