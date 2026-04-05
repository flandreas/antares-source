package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable

/**
 * A default implementation of the [DrawableDrawer] interface that simply calls the drawing method
 * of a [Drawable].
 */
class DefaultDrawableDrawer<T : Drawable> : AbstractDrawableDrawer<T>() {

    override fun process(context: DrawContext, drawable: T) {
        drawable.draw(context)
        nextProcessor(context, drawable)
    }
}