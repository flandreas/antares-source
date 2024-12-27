package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AbstractAnalogVertice
import ch.scorpion.antares.model.analog.AnalogCircuitInOut
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementProxy
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
import ch.scorpion.jabbah.edit.model.text.Alignment
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.port.PortView
import kotlin.math.max

class AnalogCircuitInOutView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogCircuitInOut = AnalogCircuitInOut(),
	eventBus: EventBus = BaseModule.eventBus,
	orientation: Direction = Direction.EAST,
	private val analogElement: AnalogElementProxy = AnalogElementProxy()
) : AbstractCircuitInOutView<AnalogCircuitInOut>(styleProvider, model, eventBus, orientation), AnalogElement by analogElement {

	val voltageLabel = Label(
		"0.0 V",
		styleProvider.getStyle(StyleType.ANNOTATION).font,
		textColor,
		HorizontalAlignment.CENTER,
		VerticalAlignment.CENTER,
		richText = false)

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: AnalogCircuitInOut?) {
		super.modelExchanged(oldModel)
		analogElement.bind(model)
		updateView()
		updateVoltageLabel()
	}

	/** ---- [AbstractCircuitInOutView] */

	override fun handleStateChangedImpl(event: GraphElementEvent) {
		if (event.signalHandler != null) {
			if (event.reason == AbstractAnalogVertice.REQUEST_REANALYZE) {
				if (parent is AnalogGraphView) {
					(parent as AnalogGraphView).recalculate(event.signalHandler!!, true)
				}
			} else {
				updateVoltageLabel()
			}
		}
	}

	private fun updateVoltageLabel() {
		val v = model.signal?.voltage

		voltageLabel.text = if (v != null) {
			"${AnalogSignal.roundVoltage(v)} V"
		} else {
			"${Bit.ALL_UNDEFINED_CHAR}"
		}
	}

	override fun updateViewImpl() {
		arrowPath = ArrowPath.Companion.Builder(
			orientation,
			Dimension2D(max(25.0, voltageLabel.bounds.width), voltageLabel.bounds.height)
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
		if (model.signal == null) {
			drawEdited(
				context,
				transparent.applyTo(Bit.Undefined.color.foregroundColor),
				transparent.applyTo(Bit.Undefined.color.backgroundColor)
			)
		} else {
			drawEdited(
				context,
				transparent.applyTo(foregroundColor),
				transparent.applyTo(backgroundColor)
			)
		}

		if (model.signal == null) {
			context.g.color = Bit.Undefined.color.textColor
		} else {
			context.g.color = textColor
		}
		context.translated(getArrowPathTranslation()) {
			voltageLabel.draw(context)
			drawFocus(context)
		}
	}

	override fun toggle(undefine: Boolean, context: ActorInteractionContext): ActorInteractionHandler? {
		model.toggle(context.signalHandler)
		requestFocus()
		return null
	}

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		updateVoltageLabel()
	}

	/** ---- [AnalogCircuitInOutView] */

	private val voltageLabelCenter: Point2D get() =
		if (orientation.isHorizontal()) {
			arrowPath!!.path.boundingBox.center.addX(-ArrowPath.ARROW_SIZE / 2)
		} else {
			arrowPath!!.path.boundingBox.center.addY(ArrowPath.ARROW_SIZE / 2)
		}

	override fun drawFocus(context: DrawContext) {
		if (isFocusOwner) {
			context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
			context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
			context.g.draw(Rectangle2D(arrowPath!!.path.boundingBox).expandBy(3.0))
		}
	}

	override fun createActorInteractionHandler(): ToggleInteractionHandler = InteractionHandler()

	private inner class InteractionHandler : ToggleInteractionHandler() {

		override fun canConsume(keyEvent: KeyEvent): Boolean {
			return when (keyEvent.key) {
				KeyEvent.VK_SPACE -> false
				else -> super.canConsume(keyEvent)
			}
		}

		override fun keyPressed(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
			if (checkTopLevelKey()) {
				when (context.keyEvent?.key) {
					KeyEvent.VK_ENTER -> toggle(false, context)
					KeyEvent.VK_Z -> model.setIncomingSignal(AnalogSignal.UNDEFINED, context.signalHandler)
					KeyEvent.VK_0 -> model.setIncomingSignal(AnalogSignal.ZERO, context.signalHandler)
					KeyEvent.VK_5 -> model.setIncomingSignal(AnalogSignal.HIGH, context.signalHandler)
				}
			}
			return null
		}
	}
}