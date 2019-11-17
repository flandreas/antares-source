package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

/**
 * Base view implementation for [AbstractDigitalGate] views.
 *
 * Must be declared public in order to support [ComponentPropertyPanel property access.
 * @param T the type of gate model displayed by this view.
 */
abstract class AbstractDigitalGateView<T : AbstractDigitalGate>(
	styleProvider: StyleProvider,
	text: String,
	vertice: T
) : BoxGateView<T>(styleProvider, text, vertice) {

	var chosenInputCount: InputCount
		get() = model!!.chosenInputCount
		set(value) {
			invalidate()
			model!!.chosenInputCount = value
			modelExchanged(model)
		}

	var outputPortName: String?
		get() = model!!.getOutput<DigitalSignal>().name
		set(value) {
			invalidate()
			model!!.getOutput<DigitalSignal>().name = value
			invalidate()
		}

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: T?) {
		super.modelExchanged(oldModel)

		for ((index, inputPort) in model!!.getInputs().withIndex()) {
			addPortView(createInputPortView(inputPort as Port<DigitalSignal>, index))
		}
		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getOutput(),
			direction = Direction.EAST,
			portLabelPosition = PortLabelPosition.EXTERNAL))
		updateLayout()
	}

	/** ---- [AbstractDigitalGateView] */

	protected open fun createInputPortView(inputPort: Port<DigitalSignal>, index: Int): PortView<*> {
		return DigitalPortView(
			styleProvider = styleProvider,
			port = inputPort,
			direction = Direction.WEST)
	}
}