package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.app.DigitalGraphViewService
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.Port
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

	/** Use [DigitalGraphViewService] for changing this value.*/
	val chosenInputCount: InputCount get() = model.chosenInputCount

	var outputPortName: String?
		get() = model.getOutput<DigitalSignal>().name
		set(value) {
			invalidate()
			model.getOutput<DigitalSignal>().name = value
			invalidate()
		}

	/** ---- [AbstractVerticeView] */

	override fun modelExchanged(oldModel: T?) {
		super.modelExchanged(oldModel)

		for (inputPort in model.getInputs()) {
			addPortView(createInputPortView(inputPort as Port<DigitalSignal>))
		}
		addPortView(createOutputPortView(model.getOutput()))

		updateLayout()
	}
}