package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.input.AbstractSwitch
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementMixin
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData

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

				if (data.graphView is AnalogGraphView) {
					(data.graphView as AnalogGraphView).requireAnalysis()
					(data.graphView as AnalogGraphView).requestActing(signalHandler)
				}
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
		propagationDelay = 0
	}

	/** ---- [AnalogElement] */

	override val voltageSourceCount: Int get() = if (isOn) 1 else 0

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		if (isOn) {
			analysis.stampVoltageSource(analogElement.nodes[0], analogElement.nodes[1], analogElement.voltageSource, 0.0)
		}
	}

	override fun calculateCurrent() { }

}