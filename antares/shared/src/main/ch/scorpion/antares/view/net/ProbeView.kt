package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Probe
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.signal.AbstractNumberViewComponent
import ch.scorpion.antares.view.signal.DigitalSignalSourceControlView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource


/**
 * A view of a [Probe].
 */
class ProbeView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    probe: Probe = Probe(),
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractNumberViewComponent<Probe>(styleProvider, probe, Direction.EAST), ControlViewSource<Probe> {

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
	    updatePortViews()
		updateView()
    }

	private fun updatePortViews() {
		clearPortViews()

		val inputPortView = DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getInput(),
			direction = Direction.WEST)
		addPortView(inputPortView)

		if (hasOutput) {
			val outputPort = DigitalPortView(
				styleProvider = styleProvider,
				port = model!!.getOutput(),
				direction = Direction.EAST)
			addPortView(outputPort)
		}
	}

    /** ---- UI properties */

    var hasOutput: Boolean
        get() = model!!.hasOutput
        set(value) {
            if (value == hasOutput) {
                return
            }

            invalidate()
	        model!!.hasOutput = value
            updatePortViews()
            updateView()
        }

	var name: String?
		get() = model!!.name
		set(value) {
			if (model!!.name != value) {
				model!!.name = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

	/** ---- [AbstractNumberViewComponent] */

    override var bitWidth: BitWidth
        get() = model!!.bitWidth
        set(value) {
            if (value != bitWidth) {
	            clear()
                model!!.bitWidth = value
                updateView()
	            postControlViewSourceChangeEvent(eventBus)
            }
        }

	override var signalRepresentation: DigitalSignalRepresentation
		get() = super.signalRepresentation
		set(value) {
			if (super.signalRepresentation != value) {
				super.signalRepresentation = value
				postControlViewSourceChangeEvent(eventBus)
			}
		}

    override val signal: DigitalSignal get() = model!!.signal!!

    override val upperLeftBoundsEdge: Point2D
        get() = when(orientation) {
            Direction.EAST -> Point2D(DigitalPortView.LENGTH.toDouble(), -numberView!!.height / 2 - insets)
            Direction.NORTH -> Point2D(-numberView!!.width / 2 - insets, -DigitalPortView.LENGTH - numberView!!.height - 2 * insets)
            Direction.SOUTH -> Point2D(-numberView!!.width / 2  - insets, DigitalPortView.LENGTH.toDouble())
            Direction.WEST -> Point2D(-DigitalPortView.LENGTH - numberView!!.width - 2 * insets, -numberView!!.height / 2 - insets)
        }

    override fun updateViewImpl() {
        getInput().direction = orientation.opposite()
        getInput().setLocation(DigitalPortView.LENGTH * orientation.dx, DigitalPortView.LENGTH * orientation.dy)
	    if (hasOutput) {
		    getOutput().direction = orientation
		    when(orientation) {
			    Direction.EAST -> getOutput().setLocation(2 * DigitalPortView.LENGTH + numberView!!.width , 0.0)
			    Direction.NORTH -> getOutput().setLocation(0.0, -2 * DigitalPortView.LENGTH - numberView!!.height )
			    Direction.WEST -> getOutput().setLocation(-2 * DigitalPortView.LENGTH - numberView!!.width, 0.0)
			    Direction.SOUTH -> getOutput().setLocation(0.0, 2 * DigitalPortView.LENGTH + numberView!!.height)
		    }
	    }
    }

    /** ---- [ControlViewSource] */

    override val controlId: String get() = "probe:$id"

    override val controlName: String get() = if (StringUtils.isBlank(name)) "$type (ID:$id)" else "$type $name"

    override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

    override fun createControlView(): ControlView<Probe> {
        val controlView = DigitalSignalSourceControlView(styleProvider, controlId, signalRepresentation, model)
        controlView.location = Point2D(0, 0)
        return controlView
    }


    /** ---- [AbstractDrawable] */

    override fun drawImpl(context: DrawContext) {
	    if (shadow) {
		    DropShadow.draw(context, transparency) {
			    context.g.fillRoundRect(xInt, yInt, width.toInt(), height.toInt(), 10, 10)
		    }
	    }
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

	    if (hasOutput) {
	        context.g.translate(getOutput().locationX, getOutput().locationY)
	        context.g.rotate(orientation.rotation.angle)
	        context.g.fill(TRIANGLE_PATH)
	        context.g.rotate(-orientation.rotation.angle)
	        context.g.translate(-getOutput().locationX, -getOutput().locationY)
        }

        context.g.color = oldColor
        context.g.stroke = oldStroke
    }
}