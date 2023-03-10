package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogCircuitInOut
import ch.scorpion.antares.view.inout.AbstractCircuitInOutView
import ch.scorpion.antares.view.inout.ArrowPath
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Focusable
import ch.scorpion.jabbah.edit.model.text.Alignment
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.port.PortView

class AnalogCircuitInOutView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogCircuitInOut = AnalogCircuitInOut(),
	eventBus: EventBus = BaseModule.eventBus,
	orientation: Direction = Direction.EAST
) : AbstractCircuitInOutView<AnalogCircuitInOut>(styleProvider, model, eventBus, orientation) {

	private val voltageLabel = Label(
		"0.0 V",
		styleProvider.getStyle(StyleType.ANNOTATION).font,
		textColor,
		HorizontalAlignment.CENTER,
		VerticalAlignment.CENTER)

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: AnalogCircuitInOut?) {
		super.modelExchanged(oldModel)
		updateView()
	}

	/** ---- [AbstractCircuitInOutView] */

	override fun handleStateChangedImpl(event: GraphElementEvent) {
		updateVoltageLabel()
	}

	private fun updateVoltageLabel() {
		val v = (model.signal.voltage * 10).toInt() / 10.0
		voltageLabel.text = "$v V"
	}

	override fun updateViewImpl() {
		arrowPath = ArrowPath.Companion.Builder(
			orientation,
			Dimension2D(voltageLabel.bounds.width, voltageLabel.bounds.height)
		).build(inout = portType === PortType.INOUT)

		voltageLabel.location = voltageLabelCenter
	}

	override fun createPortViewImpl(template: PortView<*>?, direction: Direction): PortView<*> =
		AnalogPortView(
			styleProvider,
			model.getPort(),
			direction = direction,
			length = template?.length,
			customUnconnectedLength = template?.customUnconnectedLength
		)

	override fun updateOutputLabel() {
		label.text = StringUtils.orEmpty(name)
		label.location = orientation.multiply(LABEL_DIST.toDouble())
		label.alignment = Alignment.forOrientation(orientation.opposite())
		updateBoundingBox()
	}

	override fun drawSimulated(context: DrawContext) {
		drawEdited(context,
			transparent.applyTo(foregroundColor),
			transparent.applyTo(backgroundColor))

		val translation = getArrowPathTranslation()
		context.g.translate(translation.x, translation.y)
		voltageLabel.draw(context)
		drawFocus(context)
		context.g.translate(-translation.x, -translation.y)
	}

	override fun toggle(undefine: Boolean, context: ActorInteractionContext): ActorInteractionHandler? {
		model.toggle(context.signalHandler, (context.view as DrawingView<*>).drawing as GraphView)
		requestFocus()
		return null
	}

	/** ---- [AnalogCircuitInOutView] */

	private val voltageLabelCenter: Point2D get() =
		if (orientation.isHorizontal()) {
			arrowPath!!.path.boundingBox.center.addX(-ArrowPath.ARROW_SIZE / 2)
		} else {
			arrowPath!!.path.boundingBox.center.addY(ArrowPath.ARROW_SIZE / 2)
		}

	private fun drawFocus(context: DrawContext) {
		if (isFocusOwner) {
			context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
			context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
			context.g.draw(Rectangle2D(arrowPath!!.path.boundingBox).expandBy(3.0))
		}
	}

	override fun createActorInteractionHandler(): ActorInteractionHandler = InteractionHandler()

	private inner class InteractionHandler : ToggleInteractionHandler() {
		override fun keyPressed(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			if (context.keyEvent?.key == KeyEvent.VK_ENTER && checkTopLevelKey()) {
				toggle(false, context)
			}
			return null
		}
	}
}