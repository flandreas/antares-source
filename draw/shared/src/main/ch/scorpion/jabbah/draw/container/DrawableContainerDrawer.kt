package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.drawable.AbstractDrawableDrawer
import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.draw.style.StyleType

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