package io.antarescircuit.jabbah.draw.container

import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawableContainer
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawableDrawer
import io.antarescircuit.jabbah.draw.style.Stylable
import io.antarescircuit.jabbah.draw.style.StyleType

/**
 * Used to draw [Drawable]s of a [DrawableContainer] by omitting all [Stylable]s
 * whose [StyleType.isBackdrop] is set.
 */
open class DrawableContainerDrawer<T: Drawable> : AbstractDrawableDrawer<T>() {

    override fun process(context: DrawContext, drawable: T) {
        draw(drawable, context)
        nextProcessor(context, drawable)
    }

    protected open fun draw(drawable: Drawable, context: DrawContext) {
        if (drawable !is Stylable || !drawable.styleType.isBackdrop) {
            drawable.draw(context)
        }
    }
}