package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.draw.*

/**
 * Forwards interaction initiating input events to the [Drawable] at the current mouse location of a
 * [DrawableContainer].
 */
open class DrawableContainerInputEventHandler<T: Drawable, C : InputEventContext>(val container: DrawableContainer<T>)
    : InputEventHandlerAdapter<C>() {

    override fun mouseMoved(context: C): InputEventHandler<C>? {
        val drawable = container.getDrawableAt(context.x, context.y)
        if (drawable != null) {
            return drawable.getInputEventHandler(context).mouseMoved(context)
        }
        return null
    }

    override fun mousePressed(context: C): InputEventHandler<C>? {
        val drawable = container.getDrawableAt(context.x, context.y)
        if (drawable != null) {
            return drawable.getInputEventHandler(context).mousePressed(context)
        }
        return null
    }

    override fun mouseClicked(context: C): InputEventHandler<C>? {
        val drawable = container.getDrawableAt(context.x, context.y)
        if (drawable != null) {
            return drawable.getInputEventHandler(context).mouseClicked(context)
        }
        return null
    }
}