package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Constant
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.signal.AbstractNumberViewComponent
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.draw.graphics.Color


/**
 * A view of a [Constant].
 */
class ConstantView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Constant = Constant()
) : AbstractNumberViewComponent<Constant>(styleProvider, "library.element.Constant", model, Direction.WEST) {

    init {
        modelExchanged(null)
    }

    override fun modelExchanged(oldModel: Constant?) {
        super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getOutput(),
			direction = Direction.WEST)
		addPortView(portView)
		updateView()
    }

    /** ---- UI properties */

    var value: Long
        get() = model!!.value.getValue()
        set(newValue) {
            model!!.value = Word.of(bitWidth, newValue)
        }

    /** ---- [AbstractNumberViewComponent] */

    override var bitWidth: BitWidth
        get() = model!!.bitWidth
        set(value) {
            invalidate()
            model!!.bitWidth = value
            updateView()
        }

    override val signal: DigitalSignal get() = model!!.value

    override val upperLeftBoundsEdge: Point2D
        get() = when(orientation) {
            Direction.EAST -> Point2D(getOutput().length.toDouble(), -numberView!!.height / 2 - insets)
            Direction.NORTH -> Point2D(-numberView!!.width / 2 - insets, -getOutput().length - numberView!!.height - 2 * insets)
            Direction.SOUTH -> Point2D(-numberView!!.width / 2 - insets, getOutput().length.toDouble())
            Direction.WEST -> Point2D(-getOutput().length - numberView!!.width - 2 * insets, -numberView!!.height / 2 - insets)
        }

    override fun updateViewImpl() {
        getOutput().direction = orientation.opposite()
        getOutput().setLocation(
                getOutput().length * -getOutput().direction.dx,
                getOutput().length * -getOutput().direction.dy)
    }

    /** ---- [AbstractVerticeView] */

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)
        drawBody(context)
        drawNumberView(context, context.appContext as ApplicationMode? === ApplicationMode.EXECUTE)
    }

    private fun drawBody(context: DrawContext) {
        if (context.useContextColors) {
            drawBody(context, context.color!!.foregroundColor, context.color!!.backgroundColor)
        } else {
            drawBody(context, foregroundColor, if (filled) backgroundColor else null)
        }
    }

    private fun drawBody(context: DrawContext, lineColor: Color, fillColor: Color?) {
        val oldStroke = context.g.stroke
        val oldColor = context.g.color

        if (fillColor != null) {
            context.g.color = fillColor
            context.g.fillRect(xInt, yInt, width.toInt(), height.toInt())
        }
        context.g.color = lineColor
        context.g.stroke = stroke
        context.g.drawRect(xInt, yInt, width.toInt(), height.toInt())

        context.g.stroke = oldStroke
        context.g.color = oldColor
    }
}