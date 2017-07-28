package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.drawable.AbstractRectangularUnzoomable
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.EditInputEventContext

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
        return handler as InputEventHandler<T>
    }
}