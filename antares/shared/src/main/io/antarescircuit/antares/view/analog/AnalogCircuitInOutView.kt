package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AbstractAnalogVertice
import io.antarescircuit.antares.model.analog.AnalogCircuitInOut
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.Digital2AnalogAdapter
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementProxy
import io.antarescircuit.antares.view.inout.AbstractCircuitInOutView
import io.antarescircuit.antares.view.inout.ArrowPath
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.text.Alignment
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.port.PortView
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
		null,
		HorizontalAlignment.CENTER,
		VerticalAlignment.CENTER,
		richText = false)

	/** ----  UI properties */

	@Suppress("unused") // Reflection
	var outputResistance: Long?
		get() = model.outputResistance
		set(value) {
			model.outputResistance = value
		}

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
		updateBoxes()
	}

	override fun drawSimulated(context: DrawContext) {
		val digitalSignal = Digital2AnalogAdapter.convertOutgoingSignal(model.signal)
		if (model.signal == null) {
			drawEdited(
				context,
				transparent.applyTo(Bit.Undefined.color.foregroundColor),
				transparent.applyTo(Bit.Undefined.color.backgroundColor)
			)
		} else {
			drawEdited(
				context,
				transparent.applyTo(digitalSignal.color.backgroundColor),
				transparent.applyTo(digitalSignal.color.foregroundColor))
		}

		if (model.signal == null) {
			context.g.color = Bit.Undefined.color.textColor
		} else {
			context.g.color = digitalSignal.color.textColor
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
			if (isFocusOwner && checkTopLevelKey()) {
				when (context.keyEvent?.key) {
					KeyEvent.VK_ENTER -> toggle(false, context)
					KeyEvent.VK_Z -> model.setIncomingSignal(AnalogSignal.UNDEFINED, context.signalHandler)
					KeyEvent.VK_0 -> model.setIncomingSignal(AnalogSignal.ZERO_VOLTAGE, context.signalHandler)
					KeyEvent.VK_5 -> model.setIncomingSignal(AnalogSignal.HIGH_VOLTAGE, context.signalHandler)
				}
			}
			return null
		}
	}
}