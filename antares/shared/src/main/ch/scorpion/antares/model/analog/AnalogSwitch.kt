package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.input.AbstractSwitch
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.InteractableVertice

/**
 * The electrical [resistance] of [AnalogSwitch] depends on the state of [isOn].
 */
class AnalogSwitch(
	private val analogElement: AnalogElementMixin = AnalogElementMixin()
) : AbstractSwitch<AnalogSwitch>(CALCULATOR),
	ResistingAnalogVertice,
	AnalogTwoPortVertice,
	AnalogElement by analogElement
{
	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.AnalogSwitch"

		private val CALCULATOR = Calculator()

		private class Calculator : AbstractSwitch.Companion.AbstractSwitchCalculator<AnalogSwitch>() {
			override fun calculate(vertice: AnalogSwitch, data: GraphActorData, signalHandler: SignalHandler) {
				super.calculate(vertice, data, signalHandler)
				vertice.requestAnalogGraphReanalization(signalHandler)
			}
		}
	}

	override val type: String get() = Translations.getString("${BASE_RESOURCE_KEY}.name")

	override val typeDesc: String? get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

	override val resistance: Double get() = if (isOn) 0.0 else 100_000_000.0

	init {
		analogElement.bindAnalogElement(this)
		addPort(AnalogPort())
		addPort(AnalogPort())
		propagationDelay = LongValueImpl.ZERO
	}

	private fun requestAnalogGraphReanalization(signalHandler: SignalHandler) {
		stateChanged(signalHandler, AbstractAnalogVertice.REQUEST_REANALYZE)
	}

	/** ---- [InteractableVertice] interface */

	override val interactivePropagationDelay: Long get() = propagationDelay.value

	/** ---- [AnalogElement] */

	override val voltageSourceCount: Int get() = if (isOn) 1 else 0

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		if (isOn) {
			analysis.stampVoltageSource(analogElement.nodes[0], analogElement.nodes[1], analogElement.voltageSource, 0.0)
		}
	}

	override fun calculateCurrent() { }
}