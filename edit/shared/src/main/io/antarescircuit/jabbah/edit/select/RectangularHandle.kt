package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangularUnzoomable
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.EditInputEventContext

/**
 * A [Handle] with a rectangular shape.
 *
 * [RectangularHandle] draws itself always in the same size, independent of the current zoom factor.
 */
class RectangularHandle(val handler: InputEventHandler<EditInputEventContext>
): AbstractRectangularUnzoomable(DrawModule.properties.getInt(Handle.PROP_SIZE_HALF).toDouble()), Handle {

    override val lineWidth: Double = DrawModule.properties.getStroke(Handle.PROP_STROKE).width.toDouble()

    override fun draw(context: DrawContext) {
        val rect = getViewRectangle()
        context.g.color = DrawModule.properties.getColor(Handle.PROP_FILL_COLOR)
        context.g.fill(rect)
        context.g.color = DrawModule.properties.getColor(Handle.PROP_BORDER_COLOR)
        context.g.stroke = DrawModule.properties.getStroke(Handle.PROP_STROKE)
        context.g.draw(rect)
    }

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
	    @Suppress("UNCHECKED_CAST")
        return handler as InputEventHandler<T>
    }
}