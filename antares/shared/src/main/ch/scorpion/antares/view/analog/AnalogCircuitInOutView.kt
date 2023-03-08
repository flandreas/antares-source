package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogCircuitInOut
import ch.scorpion.antares.view.inout.AbstractCircuitInOutView
import ch.scorpion.antares.view.inout.ArrowPath
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.DrawingView
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
		isFocusable = true
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

		voltageLabel.location = arrowPath!!.path.boundingBox.center
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
		context.g.translate(-translation.x, -translation.y)
	}

	override fun toggle(undefine: Boolean, context: ActorInteractionContext): ActorInteractionHandler? {
		model.toggle(context.signalHandler, (context.view as DrawingView<*>).drawing as GraphView)
		return null
	}
}