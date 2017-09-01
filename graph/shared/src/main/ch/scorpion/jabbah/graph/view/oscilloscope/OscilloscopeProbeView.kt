package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

class OscilloscopeProbeView(
        location: Point2D,
        rowNumber: Int,
        private val color: CompositeColor,
        private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangle(location.x, location.y, SIZE, SIZE) {

    companion object {
        val SIZE = 42.0

        private val PATH = System.get().createPath()
                .moveTo(0.0, 42.0)
                .lineTo(7.0, 21.0)
                .curveTo(16.0, 0.0, 42.0, 28.0, 21.0, 35.0)
                .close()
    }

    private val label = Label(
            text = rowNumber.toString(),
            font = styleProvider.getStyle(GraphStyleType.VERTICE).font,
            location = Point2D(18, 26)
    )

    private val handler = Handler()

    private var isHovering = false

    /** ---- [Drawable] */

    override fun draw(context: DrawContext) {
        context.g.translate(x, y)

        if (isHovering) {
            // TODO Configurable
            context.g.color = Color.ORANGE
            context.g.draw(PATH)
            label.draw(context)
        } else {
            context.g.color = color.backgroundColor
            context.g.fill(PATH)
            context.g.color = color.foregroundColor
            context.g.stroke = styleProvider.getStyle(GraphStyleType.ANNOTATION).stroke
            context.g.draw(PATH)
            //context.g.color = Color.BLACK
            label.draw(context)
        }

        context.g.translate(-x, -y)
    }

    override val lineWidth: Double get() = 0.0

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return handler
    }

    /** ---- [OscilloscopeProbeView] */

    var rowNumber: Int = rowNumber
        set(value) {
            if (field != value) {
                field = value
                label.text = field.toString()
            }
        }

    private inner class Handler : InputEventHandlerAdapter<InputEventContext>() {
        override fun mouseMoved(context: InputEventContext): InputEventHandler<InputEventContext>? {
            // TODO Refactoring: Copy/Paste from IconButton. Extract hovering behaviour
            if (contains(context.x, context.y)) {
                if (!isHovering) {
                    isHovering = true
                    invalidate()
                    validate()
                }
                return this
            }
            if (isHovering) {
                isHovering = false
                invalidate()
                validate()
            }
            return null
        }
    }

}