package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Probe
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.signal.AbstractNumberViewComponent
import ch.scorpion.antares.view.signal.DigitalSignalSourceControlView
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.GraphApplicationContext


/**
 * A view of a [Probe].
 */
class ProbeView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    probe: Probe = Probe()
) : AbstractNumberViewComponent<Probe>(styleProvider, "library.element.Probe", probe, Direction.EAST), ControlViewSource<Probe> {

    companion object {
        const val PROP_ICON_PATH = "ch.scorpion.antares.view.net.ProbeView.iconPath"
        private val TRIANGLE_PATH = System.get().createPath()
            .moveTo(0, 0)
            .lineTo(0, 5)
            .lineTo(8, 0)
            .lineTo(0, -5)
            .close()
    }

    init {
        modelExchanged(null)
    }

    override fun modelExchanged(oldModel: Probe?) {
        super.modelExchanged(oldModel)

        val inputPortView = DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getInput(),
			direction = Direction.WEST)
		addPortView(inputPortView)

		hasOutput = model!!.hasOutput
		updateView()
    }

    /** ---- UI properties */

    var hasOutput: Boolean
        get() = model!!.hasOutput
        set(value) {
            if (value == hasOutput) {
                return
            }

            invalidate()
            if (value) {
                model!!.hasOutput = true
                val outputPort = DigitalPortView(
                    styleProvider = styleProvider,
                    port = model!!.getOutput(),
                    direction = Direction.WEST)
                addPortView(outputPort)
            } else {
                removePortView(getOutput())
                model!!.hasOutput = false
            }
            updateView()
        }

    /** ---- [AbstractNumberViewComponent] */

    override var bitWidth: BitWidth
        get() = model!!.bitWidth
        set(value) {
            if (value != bitWidth) {
	            clear()
                model!!.bitWidth = value
                updateView()
            }
        }

    override val signal: DigitalSignal get() = model!!.signal!!

    override val upperLeftBoundsEdge: Point2D
        get() = when(orientation) {
            Direction.EAST -> Point2D(getInput().length.toDouble(), -numberView!!.height / 2 - insets)
            Direction.NORTH -> Point2D(-numberView!!.width / 2 - insets, -getInput().length - numberView!!.height - 2 * insets)
            Direction.SOUTH -> Point2D(-numberView!!.width / 2  - insets, getInput().length.toDouble())
            Direction.WEST -> Point2D(-getInput().length - numberView!!.width - 2 * insets, -numberView!!.height / 2 - insets)
        }

    override fun updateViewImpl() {
        getInput().direction = orientation.opposite()
        getInput().setLocation(getInput().length * orientation.dx, getInput().length * orientation.dy)
    }

    /** ---- [ControlViewSource] */

    override val controlId: String get() = "probe:$id"

    override val controlName: String get() = "$type $id"

    override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

    override fun createControlView(): ControlView<Probe> {
        val controlView = DigitalSignalSourceControlView(styleProvider, controlId, signalRepresentation, model)
        controlView.location = Point2D(0, 0)
        return controlView
    }


    /** ---- [AbstractDrawable] */

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)
        if (ApplicationMode.EXECUTE === context.castedAppContext<GraphApplicationContext>()!!.mode) {
            drawSimulated(context)
        } else {
            drawEdited(context)
        }
    }

    /** ---- [ProbeView] */

    private fun drawSimulated(context: DrawContext) {
        drawEdited(context)
        drawNumberView(context, true)
    }

    private fun drawEdited(context: DrawContext) {
        if (context.useContextColors) {
            drawEdited(context, context.color!!.foregroundColor, context.color!!.backgroundColor)
        } else {
            drawEdited(context, foregroundColor, if (filled) backgroundColor else null)
        }
    }

    private fun drawEdited(context: DrawContext, lineColor: Color, fillColor: Color?) {
        val oldStroke = context.g.stroke
        val oldColor = context.g.color

        if (fillColor != null) {
            context.g.color = fillColor
            context.g.fillRoundRect(xInt, yInt, width.toInt(), height.toInt(), 10, 10)
        }
        context.g.stroke = stroke
        context.g.color = lineColor
        context.g.drawRoundRect(xInt, yInt, width.toInt(), height.toInt(), 10, 10)
        context.g.stroke = oldStroke

        if (hasOutput) {
            context.g.translate(
                getInput().length.toDouble() * orientation.dx,
                getInput().length.toDouble() * orientation.dy)
            context.g.rotate(orientation.rotation.angle)

            context.g.fill(TRIANGLE_PATH)

            context.g.rotate(-orientation.rotation.angle)
            context.g.translate(
                -getInput().length.toDouble() * orientation.dx,
                -getInput().length.toDouble() * orientation.dy)
        }

        context.g.color = oldColor
        context.g.stroke = oldStroke
    }
}