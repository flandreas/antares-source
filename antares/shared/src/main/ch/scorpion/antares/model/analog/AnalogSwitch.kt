package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.input.AbstractSwitch
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.DummyAnalogCircuitCalculator
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData

class AnalogSwitch : AbstractSwitch<AnalogSwitch>(CALCULATOR) {

	companion object {
		private val LOG by logger(AnalogSwitch::class)
		private const val BASE_RESOURCE_KEY = "library.element.AnalogSwitch"

		private val CALCULATOR = Calculator()

		private class Calculator : AbstractSwitch.Companion.AbstractSwitchCalculator<AnalogSwitch>() {
			override fun calculate(vertice: AnalogSwitch, data: GraphActorData, signalHandler: SignalHandler) {
				LOG.debug("Calculating AnalogSwitch")
				super.calculate(vertice, data, signalHandler)

				if (data.graphView is AnalogGraphView) {
					DummyAnalogCircuitCalculator.calculate(data.graphView as AnalogGraphView, signalHandler)
				}
			}
		}
	}

	override val type: String get() = Translations.getString("${BASE_RESOURCE_KEY}.name")

	override val typeDesc: String? get() = Translations.getOptionalString("${BASE_RESOURCE_KEY}.desc")

	init {
		addPort(AnalogPort())
		addPort(AnalogPort())
		propagationDelay = 0
	}
}